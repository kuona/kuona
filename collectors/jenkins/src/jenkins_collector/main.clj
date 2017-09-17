(ns jenkins-collector.main
  (:require [clj-http.client :as client]
            [clojure.string :as string]
            [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [jenkins-collector.config :as config]
            [kuona-core.metric.store :as store]
            [kuona-core.util :as util]
            [jenkins-collector.jenkins :as jenkins])
  (:gen-class))


(def cli-options
  [["-j" "--jenkins url" "Url for the jenkins server to query"]
   ["-u" "--username name" "Jenkins username to user for basic authentication"]
   ["-p" "--password password" "Jenkins password for the supplied uername" ]
   ["-a" "--api-url URL" "The URL of the back end Kuona API" :default "http://dashboard.kuona.io"]
   ["-c" "--config FILE" "Configuration file for CLI options" :default "local-properties.edn"]
   ["-h" "--help"]])

(defn usage
  [options-summary]
  (->> ["Kuona Jenkins build collector."
        ""
        "Usage: lein run -- [options]"
        ""
        "Options:"
        options-summary
        ""]
       (string/join \newline)))


(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) {:exit-message (usage summary) :ok? true}
      errors {:exit-message (error-msg errors)}
      :else options)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn options-to-configuration
  [options]
  {})

(defn configure
  [args]
  (let [options (validate-args args)]
    (cond
      (contains? options :exit-message) (exit (if (:ok? options) 0 1) (:exit-message options))
      :else
      (merge options (util/load-config (-> options :config)) options))))



(defn config-file-stream
  [path]
  (clojure.java.io/reader path))

(defn updated-collect-metrics
  [mapping config]
  (let [metrics (jenkins/collect-metrics (jenkins/http-source (:credentials config)) (:url config))]
    (doall (map #(store/put-document % mapping) metrics))))

(defn -main
  [& args]
  (let [config       (configure args)
        source       (jenkins/http-source {:username (-> config :username) :password (-> config :password)})
        build-server (:jenkins config)
        api (-> config :api-url)]
    (log/info "Collecting metrics from:" build-server)
    (let [metrics (jenkins/collect-metrics source build-server)]
      (jenkins/upload-metrics metrics api))))
;  (let [options     (parse-opts args cli-options)
;        config-file (:config (:options options))
;        config      (config/load-config (config-file-stream config-file))
;        index       (store/index :kuona-metrics "http://localhost:9200")
;        mapping     (store/mapping :build index)]
;    (log/info "Jenkins Collector - starting")
;    (log/info "Using configruation" config-file)
;    (if (not (store/has-index? index)) (store/create-index index store/metric-mapping-type))
;    (doall (map #(updated-collect-metrics mapping %) (:collections config))))

