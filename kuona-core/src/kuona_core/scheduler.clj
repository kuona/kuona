(ns kuona-core.scheduler
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.simple :refer [schedule repeat-forever with-repeat-count with-interval-in-milliseconds]]
            [clojure.tools.logging :as log]
            [kuona-core.metric.store :as store]
            [kuona-core.github :as github]
            [kuona-core.collector.tfs :as tfs]
            [clj-http.client :as http]
            [kuona-core.util :as util]
            [kuona-core.stores :refer [repositories-store commit-logs-store code-metric-store collector-config-store]]))

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
  (github/get-project-repositories org))


(defn repository-id [repo]
  (util/uuid-from (-> repo :url)))

(defn put-repository
  [entry]
  (log/info "put-repository")
  (clojure.pprint/pprint entry)
  (let [id (repository-id entry)]
    (store/put-document entry repositories-store id)
    ))

(defn refresh-tfs-org
  [org token]
  (let [entries (tfs/find-organization-repositories org token)]
    (doseq [entry entries] (put-repository entry)))

  )

(defn collect [e]
  (let [config (-> e :config)]
    (cond
      (github-org-collector-config? e) (refresh-github-org
                                         (-> config :org)
                                         (-> config :username)
                                         (-> config :token))
      (tfs-org-collector-config? e) (refresh-tfs-org (-> config :org) (-> config :token))
      :else (log/info "collector type not supported")
      )))

(defn refresh-repositories
  []
  (log/info "Refreshing known repositories")
  (let [url (str collector-config-store "/_search?size=100&q=collector_type:VCS")
        docs (store/find-documents url)]
    (clojure.pprint/pprint docs)
    (doall (map collect (-> docs :items)))))

(def workspace-path "/Volumes/data-drive/workspace")

(defn collect-repository-metrics []
  (log/info "Collecting metrics from known repositories")
  (let [
        repositories (store/all-documents repositories-store)
        urls (map #(-> % :url) repositories)]
    (log/info "Found " (count repositories) " configured repositories for analysis")
    (doseq [url urls] (kuona-core.git/collect commit-logs-store code-metric-store workspace-path url))))

(defn refresh-build-metrics []
  (log/info "Collecting build information from build servers"))

(defn collect-environment-metrics []
  (log/info "Collecting environment status data"))

(defjob background-data-collector
        [ctx]
        (refresh-repositories)
        (collect-repository-metrics)
        (refresh-build-metrics)
        (collect-environment-metrics))



(defn start
  []
  (let [s (-> (qs/initialize) qs/start)
        refresh-repositories-job (j/build
                                   (j/of-type background-data-collector)
                                   (j/with-identity (j/key "repositories.refresh.1")))
        trigger (t/build
                  (t/with-identity (t/key "triggers.1"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (repeat-forever)
                                     ;(with-repeat-count 10)
                                     (with-interval-in-milliseconds 10000))))]
    (qs/schedule s refresh-repositories-job trigger)
    ))
