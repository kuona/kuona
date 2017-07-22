(ns kuona-api.main
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [slingshot.slingshot :refer [try+]]
            [ring.adapter.jetty :as jetty]
            [clojure.tools.logging :as log]
            [kuona-core.metric.store :as store]
            [kuona-api.handler :as service]
            [kuona-api.config :as config]
            [kuona-api.environments :as env])
  (:gen-class))

(def cli-options
  [["-c" "--config FILE"           "API Key used to communicate with the API service"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Kuona Environment dashboard service."
        ""
        "Usage: environment-service -c configuration.json"
        ""
        "Options:"
        options-summary
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (log/info "Exiting with status " msg status)
  (println  "Exiting with status " msg status)
  (System/exit status))

(defn config-file-stream
  [path]
  (clojure.java.io/reader path))

(defn print-help-and-exit
  [code summary]
  (exit 0 (usage summary)))

(defn help-requested?
  [options]
  (:help options))

(defn wrong-options?
  [options]
  (not= (count options) 1))



(defn create-repository-index-if-missing
  []
  (let [index (store/index "kuona-repositories" "http://localhost:9200")]
    (if (store/has-index? index) nil (store/create-index index {:repositories{}}))))

(defn create-snapshot-index-if-missing
  []
  (let [index (store/index "kuona-snapshots" "http://localhost:9200")]
    (if (store/has-index? index) nil (store/create-index index {:repositories{}}))))

(defn start-application
  [config]
  (try+
;   (env/update-environment-mapping)
   (create-repository-index-if-missing)
   (create-snapshot-index-if-missing)
   (exit 0 (jetty/run-jetty #'service/app (config/load-config (config-file-stream config))))
   (catch [:type :config/missing-parameter] {:keys [parameter p]}
     (log/error "Missing configuration parameter " p))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (log/info "Starting environment service with options" options)
    (cond
      (help-requested? options) (print-help-and-exit 0 summary)
      (wrong-options? options) (print-help-and-exit 1 summary)
      errors (exit 1 (error-msg errors))
      :else (start-application (:config options)))))

