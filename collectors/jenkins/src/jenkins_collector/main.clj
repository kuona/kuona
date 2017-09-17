(ns jenkins-collector.main
  (:require [clj-http.client :as client]
            [clojure.string :as string]
            [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [kuona-core.metric.store :as store]
            [kuona-core.util :as util]
            [kuona-core.cli :as cli]
            [jenkins-collector.jenkins :as jenkins])
  (:gen-class))

(def cli-options
  [["-j" "--jenkins url" "Url for the jenkins server to query"]
   ["-u" "--username name" "Jenkins username to user for basic authentication"]
   ["-p" "--password password" "Jenkins password for the supplied uername" ]
   ["-a" "--api-url URL" "The URL of the back end Kuona API" :default "http://dashboard.kuona.io"]
   ["-c" "--config FILE" "Configuration file for CLI options" :default "local-properties.edn"]
   ["-h" "--help"]])

(defn updated-collect-metrics
  [mapping config]
  (let [metrics (jenkins/collect-metrics (jenkins/http-source (:credentials config)) (:url config))]
    (doall (map #(store/put-document % mapping) metrics))))

(defn -main
  [& args]
  (let [config       (cli/configure "Kuona Jenkins build collector." cli-options args)
        source       (jenkins/http-source {:username (-> config :username) :password (-> config :password)})
        build-server (:jenkins config)
        api (-> config :api-url)]
    (log/info "Collecting metrics from:" build-server)
    (let [metrics (jenkins/collect-metrics source build-server)]
      (jenkins/upload-metrics metrics api))))
