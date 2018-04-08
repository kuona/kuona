(ns kuona-core.scheduler
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.simple :refer [schedule repeat-forever with-repeat-count with-interval-in-milliseconds]]
            [clojure.tools.logging :as log]
            [kuona-core.metric.store :as store]))

(def collector-config (store/mapping :collector (store/index :kuona-collector-config "http://localhost:9200")))

(defn refresh-github-org
  [& {:keys [org username token]}]

  )

(defn github-org-collector-config?
  [config]
  (and
    (= (-> config :collector_type) "VCS")
    (= (-> config :collector) "GitHubOrg")))

(defjob refresh-repositories-worker
        [ctx]
        (let [url (str collector-config "/_search?size=100&q=collector_type:VCS")
              docs (store/find-documents url)]
          (clojure.pprint/pprint docs)
          (doall (map (fn [e]
                        (cond
                          (github-org-collector-config? e) (refresh-github-org (-> e :config))
                          :else (log/info "collector type not supported")
                          )) (-> docs :items)))))

(defjob kuona-worker
        [ctx]
        (log/info "worker run")
        (comment "Does nothing"))

(defn start
  []
  (let [s (-> (qs/initialize) qs/start)
        job (j/build
              (j/of-type kuona-worker)
              (j/with-identity (j/key "jobs.noop.1")))
        refresh-repositories-job (j/build
                                   (j/of-type refresh-repositories-worker)
                                   (j/with-identity (j/key "repositories.refresh.1")))
        trigger (t/build
                  (t/with-identity (t/key "triggers.1"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (repeat-forever)
                                     ;(with-repeat-count 10)
                                     (with-interval-in-milliseconds 2000))))]
    (qs/schedule s refresh-repositories-job trigger)
    ;(qs/schedule s job trigger)
    ))
