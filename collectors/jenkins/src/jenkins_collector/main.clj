(ns jenkins-collector.main
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jenkins-collector.jenkins :as jenkins]
            [kuona-core.cli :as cli]
            [kuona-core.metric.store :as store]
            [kuona-core.util :as util]
            [slingshot.slingshot :refer :all])
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


(defn log-collection
  "Wraps the collection with logging to the API server to act as a history of changes"
  [api-url collector-details params f]
  
  (try+
   (collector-log api-url collector-details params :started)
   (f)
   (collector-log api-url collector-details params :stopped)
   (catch Object _
     (collector-log api-url collector-details params :failed))))


(defn -main
  [& args]
  (let [config            (cli/configure "Kuona Jenkins build collector." cli-options args)
        username          (-> config :username)
        password          (-> config :password)
        build-server      (-> config :jenkins)
        api-url           (-> config :api-url)
        source            (jenkins/http-source {:username username :password password})
        collector-details {:name "jenkins-collector" :version (util/get-project-version 'kuona-jenkins-collector)}]

    (log/info "Collecting metrics from:" build-server)

    (log-collection api-url
                    collector-details
                    [{:api api-url} {:build-server build-server}]
                    #(jenkins/collect-metrics source
                                              build-server
                                              api-url))))

