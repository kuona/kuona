(ns kuona-core.scheduler
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.simple :refer [schedule repeat-forever with-repeat-count with-interval-in-milliseconds]]
            [clojure.tools.logging :as log]))


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
        trigger (t/build
                  (t/with-identity (t/key "triggers.1"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (repeat-forever)
                                     ;(with-repeat-count 10)
                                     (with-interval-in-milliseconds 2000))))]
    (qs/schedule s job trigger)))
