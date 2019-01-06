(ns kuona-api.core.scheduler
  (:require [clojurewerkz.quartzite.scheduler :as scheduler]
            [clojurewerkz.quartzite.triggers :as triggers]
            [clojurewerkz.quartzite.jobs :as jobs]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.simple :refer [schedule repeat-forever with-repeat-count with-interval-in-minutes]]
            [slingshot.slingshot :refer :all]
            [clojure.tools.logging :as log]
            [kuona-api.core.store :as store]
            [kuona-api.core.github :as github]
            [kuona-api.core.collector.tfs :as tfs]
            [kuona-api.core.util :as util]
            [kuona-api.core.git :as git]
            [kuona-api.core.collector.snapshot :as snapshot]
            [kuona-api.core.stores :refer [repositories-store commit-logs-store code-metric-store collector-config-store source-code-store]]
            [kuona-api.core.health-check :as health-check]
            [kuona-api.core.workspace :refer [get-workspace-path]]
            [kuona-api.core.stores :as stores]
            [kuona-api.core.collector.source-code :as source-code]))

(defn track-activity
  ([stage status]
   (track-activity stage status {}))
  ([stage status params]
   (store/put-document {:id         (util/uuid)
                        :collector  {:name    stage
                                     :version (util/get-project-version 'kuona-api)}
                        :activity   status
                        :parameters params
                        :timestamp  (util/timestamp)} stores/collector-activity-store)))

(defmacro record-activity
  [message options f]
  `(do (track-activity ~message :started ~options)
       ~f
       (track-activity ~message :completed ~options)))

(defn tfs-org-collector-config? [config]
  (and
    (= (-> config :collector_type) "VCS")
    (= (-> config :collector) "TFS")))

(defn github-org-collector-config?
  [config]
  (and
    (= (-> config :collector_type) "VCS")
    (= (-> config :collector) "GitHubOrg")))

(defn refresh-github-org
  [org username password]
  (log/info "Refreshing github organisation repositories" org)
  (record-activity "GitHub org/user collector" {:organization org}
                   (github/get-project-repositories org)))


(defn repository-id [repo]
  (util/uuid-from (-> repo :url)))

(defn put-repository!
  [entry]
  (log/info "put-repository")
  (let [id (repository-id entry)]
    (try+
      (store/put-document (merge {:id id} entry) repositories-store id)
      (catch Object _
        (log/error (:throwable &throw-context) "Unexpected error saving repository" id "entry" entry)))))

(defn refresh-tfs-org
  [org token]
  (log/info "Refreshing TFS repositories" org)

  (record-activity
    "TFS org/user collector"
    {:organization org}
    (let [entries (tfs/find-organization-repositories org token)]
      (doseq [entry entries] (put-repository! entry)))))

(defn collect-repository-data [e]
  (log/info "Collecting/refreshing repository data")
  (try+
    (let [config (-> e :config)]
      (cond
        (github-org-collector-config? e) (refresh-github-org
                                           (-> config :org)
                                           (-> config :username)
                                           (-> config :token))
        (tfs-org-collector-config? e) (refresh-tfs-org (-> config :org) (-> config :token))
        :else (log/info "collector type not supported")))
    (catch Object _
      (store/put-document {:id         (util/uuid)
                           :collector  {:name    (-> e :collector)
                                        :version (util/get-project-version 'kuona-api)}
                           :activity   :error
                           :parameters (select-keys (-> e :config) [:org :username])
                           :timestamp  (util/timestamp)} stores/collector-activity-store)
      (log/error (:throwable &throw-context) "Unexpected error collecting repository data " e))))

(defn refresh-repositories
  []
  (log/info "Refreshing known repositories")
  (record-activity "Refreshing repositories" {}
                   (let [url  (.url collector-config-store ["_search"] ["size=100" "q=collector_type:VCS"])
                         docs (store/find-documents url)]
                     (doall (map collect-repository-data (-> docs :items))))))

(defn collect-repository-metrics
  []
  (log/info "Collecting metrics from known repositories")
  (track-activity "Updating repository metrics" :started)
  (let [repositories (store/all-documents repositories-store)]
    (log/info "Found " (count repositories) " configured repositories for analysis")

    (doseq [repo repositories]
      (let [url (-> repo :url)]
        (cond
          (nil? url) (log/error "No URL field found in repository" repo)
          :else (record-activity "Repository snapshot collector"
                                 {:url url}
                                 (do
                                   (snapshot/create-repository-snapshot (git/local-clone-path (get-workspace-path) url) repo))))))

    (doseq [repo repositories]
      (let [url (-> repo :url)]
        (cond
          (nil? url) (log/error "No URL field found in repository" repo)
          :else (record-activity "Repository Commit history"
                                 {:url url}
                                 (git/collect-commit-logs commit-logs-store code-metric-store (get-workspace-path) url (-> repo :id))))))

    (doseq [repo repositories]
      (let [url (-> repo :url)]
        (cond
          (nil? url) (log/error "No URL field found in repository" repo)
          :else (record-activity "Historical code metric collector"
                                 {:url url}
                                 (git/collect-repository-historical-code-metrics1 code-metric-store (get-workspace-path) url (-> repo :id))))))
    ;(doseq [repo repositories]
    ;  (let [url (-> repo :url)]
    ;    (cond
    ;      (nil? url) (log/error "No URL field found in repository" repo)
    ;      :else (record-activity "Historical code metric collector"
    ;                             {:url url}
    ;                             (source-code/collect source-code-store (get-workspace-path) url (-> repo :id)))))
    ;  )

    )
  (track-activity "Updating respository metrics" :completed))


(defjob background-data-collector [ctx]
        (log/info "Background data collection started")
        (refresh-repositories)
        (log/info "Updating repository metrics")
        (collect-repository-metrics))

(defjob background-health-check-collector
        [ctx]
        (log/info "collecting health check data")
        (let [collection-date (util/timestamp)
              health-checks   (health-check/all-health-checks)]
          (log/info "health checks" health-checks collection-date)
          (doall (map (fn [hc] (health-check/perform-health-checks hc collection-date)) health-checks))))

(defn start
  []
  (let [s                     (-> (scheduler/initialize) scheduler/start)
        refresh-repositories  (jobs/build
                                (jobs/of-type background-data-collector)
                                (jobs/with-identity (jobs/key "repositories.refresh.1")))
        refresh-health-checks (jobs/build
                                (jobs/of-type background-health-check-collector)
                                (jobs/with-identity (jobs/key "health-check.refresh.1")))
        every-minute          (triggers/build
                                (triggers/with-identity (triggers/key "every-minute-trigger"))
                                (triggers/start-now)
                                (triggers/with-schedule (schedule
                                                          (repeat-forever)
                                                          (with-interval-in-minutes 1))))
        every-hour            (triggers/build
                                (triggers/with-identity (triggers/key "every-hour-trigger"))
                                (triggers/start-now)
                                (triggers/with-schedule (schedule
                                                          (repeat-forever)
                                                          (with-interval-in-minutes 60))))]
    (scheduler/schedule s refresh-repositories every-hour)
    (scheduler/schedule s refresh-health-checks every-minute)))
