(ns jenkins-collector.main
  (:require [clj-http.client :as http]
            [clojure.string :as string]
            [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer :all]
            [kuona-core.metric.store :as store]
            [kuona-core.util :as util]
            [kuona-core.cli :as cli]
            [jenkins-collector.jenkins :as jenkins])
  (:gen-class))

(def cli-options
  [["-j" "--jenkins url" "Url for the jenkins server to query"]
   ["-u" "--username name" "Jenkins username to user for basic authentication"]
   ["-p" "--password password" "Jenkins password for the supplied uername"]
   ["-a" "--api-url URL" "The URL of the back end Kuona API" :default "http://dashboard.kuona.io"]
   ["-c" "--config FILE" "Configuration file for CLI options" :default "local-properties.edn"]
   ["-h" "--help"]])

(defn updated-collect-metrics
  [mapping config]
  (let [metrics (jenkins/collect-metrics (jenkins/http-source (:credentials config)) (:url config))]
    (doall (map #(store/put-document % mapping) metrics))))

(def collector-details
  {:name    "jenkins-collector"
   :version (util/get-project-version 'kuona-jenkins-collector)})

(defn collector-log
  "Posts details of the collector to the API server to record the collector run"
  [api-url collector-details parameters status]
  (let [url  (string/join "/" [api-url "api" "collectors" "activities"])
        body (util/build-json-request {:id         (util/uuid)
                                       :collector  collector-details
                                       :activity   status
                                       :parameters parameters
                                       :timestamp  (util/timestamp)})]
    (http/post url body)))


(defn log-collection [api-url collector-details params f]
  (try+
   (collector-log api-url collector-details params :started)
   
   (f)
   
   (collector-log api-url collector-details params :stopped)
   (catch Object _
     (collector-log api-url collector-details params :failed))))


(defn -main
  [& args]
  (let [config       (cli/configure "Kuona Jenkins build collector." cli-options args)
        source       (jenkins/http-source {:username (-> config :username) :password (-> config :password)})
        build-server (:jenkins config)
        api-url      (-> config :api-url)]
    (log/info "Collecting metrics from:" build-server)

    (log-collection api-url collector-details [{:api api-url} {:build-server build-server}] #(jenkins/collect-metrics source build-server api-url))))

