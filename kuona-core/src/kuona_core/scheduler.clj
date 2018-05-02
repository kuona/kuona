(ns kuona-core.scheduler
  (:require [clojurewerkz.quartzite.scheduler :as scheduler]
            [clojurewerkz.quartzite.triggers :as triggers]
            [clojurewerkz.quartzite.jobs :as jobs]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.simple :refer [schedule repeat-forever with-repeat-count with-interval-in-minutes]]
            [slingshot.slingshot :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.store :as store]
            [kuona-core.github :as github]
            [kuona-core.collector.tfs :as tfs]
            [kuona-core.util :as util]
            [kuona-core.git :as git]
            [kuona-core.collector.snapshot :as snapshot]
            [kuona-core.stores :refer [repositories-store commit-logs-store code-metric-store collector-config-store]]
            [kuona-core.workspace :refer [get-workspace-path]]
            [kuona-core.stores :as stores]))

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
  (track-activity "GitHub org/user collector" :started {:organization org})
  (github/get-project-repositories org)
  (track-activity "GitHub org/user collector" :completed {:organization org}))


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
  (track-activity "TFS org/user collector" :started {:organization org})
  (let [entries (tfs/find-organization-repositories org token)]
    (doseq [entry entries] (put-repository! entry)))
  (track-activity "TFS org/user collector" :completed {:organization org}))

(defn collect-repository-data [e]
  (log/info "Collecting/refreshing repository date")
  (let [config (-> e :config)]
    (cond
      (github-org-collector-config? e) (refresh-github-org
                                         (-> config :org)
                                         (-> config :username)
                                         (-> config :token))
      (tfs-org-collector-config? e) (refresh-tfs-org (-> config :org) (-> config :token))
      :else (log/info "collector type not supported"))))


(defn refresh-repositories
  []
  (log/info "Refreshing known repositories")
  (track-activity "Refreshing repositories" :started)
  (let [url  (.url collector-config-store ["_search"] ["size=100" "q=collector_type:VCS"])
        docs (store/find-documents url)]
    (doall (map collect-repository-data (-> docs :items))))
  (track-activity "Refreshing repositories" :completed))

(defn collect-repository-metrics
  []
  (log/info "Collecting metrics from known repositories")
  (track-activity "Updating respository metrics" :started)
  (let [repositories (store/all-documents repositories-store)]
    (log/info "Found " (count repositories) " configured repositories for analysis")

    (doseq [repo repositories]
      (let [url (-> repo :url)]
        (cond
          (nil? url) (log/error "No URL field found in repository" repo)
          :else (do
                  (track-activity "Repository snapshot collector" :started {:url url})
                  (snapshot/create-repository-snapshot (git/local-clone-path (get-workspace-path) url) repo)
                  (track-activity "Repository snapshot collector" :comppleted {:url url})))))

    (doseq [repo repositories]
      (let [url (-> repo :url)]
        (cond
          (nil? url) (log/error "No URL field found in repository" repo)
          :else (do
                  (track-activity "Historical code metric collector" :started {:url url})
                  (git/collect-commits commit-logs-store code-metric-store (get-workspace-path) url (-> repo :id))
                  (track-activity "Historical code metric collector" :completed {:url url}))))))
  (track-activity "Updating respository metrics" :completed))



(defn refresh-build-metrics []
  (log/info "Collecting build information from build servers"))

(defn collect-environment-metrics []
  (log/info "Collecting environment status data"))

(defjob background-data-collector [ctx]
        (log/info "Background data collection started")
        (refresh-repositories)
        (log/info "Updating repository metrics")
        (collect-repository-metrics)
        (refresh-build-metrics)
        (collect-environment-metrics))



(defn start
  []
  (let [s                        (-> (scheduler/initialize) scheduler/start)
        refresh-repositories-job (jobs/build
                                   (jobs/of-type background-data-collector)
                                   (jobs/with-identity (jobs/key "repositories.refresh.1")))
        trigger                  (triggers/build
                                   (triggers/with-identity (triggers/key "triggers.1"))
                                   (triggers/start-now)
                                   (triggers/with-schedule (schedule
                                                             (repeat-forever)
                                                             ;(with-repeat-count 10)
                                                             (with-interval-in-minutes 30))))]
    (scheduler/schedule s refresh-repositories-job trigger)
    ))
