(ns jenkins-collector.main
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [jenkins-collector.config :as config]
            [kuona-core.metric.store :as store]
            [jenkins-collector.jenkins :as jenkins])
  (:gen-class))


(def cli-options
  [["-c" "--config FILE" "JSON formatted configuration file" :default "properties.json"]
   ["-h" "--help"]])

(defn config-file-stream
  [path]
  (clojure.java.io/reader path))

(defn updated-collect-metrics
  [mapping config]
  (let [metrics (jenkins/collect-metrics (jenkins/http-source (:credentials config)) (:url config))]
    (doall (map #(store/put-document % mapping) metrics))))

(defn -main
  [& args]
  (let [options     (parse-opts args cli-options)
        config-file (:config (:options options))
        config      (config/load-config (config-file-stream config-file))
        index       (store/index :kuona-metrics "http://localhost:9200")
        mapping     (store/mapping :build index)]
    (log/info "Jenkins Collector - starting")
    (log/info "Using configruation" config-file)
    (if (not (store/has-index? index)) (store/create-index index store/metric-mapping-type))
    (doall (map #(updated-collect-metrics mapping %) (:collections config)))))
