(ns kuona-api.main
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [slingshot.slingshot :refer [try+]]
            [ring.adapter.jetty :as jetty]
            [clojure.tools.logging :as log]
            [kuona-core.metric.store :as store]
            [kuona-api.handler :as service]
            [kuona-api.config :as config]
            [kuona-api.collector-handlers :refer [create-collectors-index-if-missing]]
            [kuona-api.environments :as env])
  (:gen-class))

(def cli-options
  [["-c" "--config FILE" "API Key used to communicate with the API service (deprecated/ignored)" :default "kuona-properties.json"]
   ["-p" "--port PORT" "API port" :default 9001]
   ["-h" "--help"]])

(defn usage
  [options-summary]
  (->> ["Kuona API service."
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
  (println  msg status)
  (System/exit status))

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

(defn file-exists?
  [file-path]
  (.exists (io/file file-path)))

(defn create-repository-index-if-missing
  []
  (let [index (store/index "kuona-repositories" "http://localhost:9200")]
    (if (store/has-index? index) nil (store/create-index index {:repositories{}}))))

(defn create-snapshot-index-if-missing
  []
  (let [index (store/index "kuona-snapshots" "http://localhost:9200")]
    (if (store/has-index? index) nil (store/create-index index {:repositories{}}))))

(defn start-application
  [port]
  (log/info "Starting API on port " port)
  (try+
   (create-repository-index-if-missing)
   (create-snapshot-index-if-missing)
   (create-collectors-index-if-missing)
   (exit 0 (jetty/run-jetty #'service/app {:port port}))
   (catch [:type :config/missing-parameter] {:keys [parameter p]}
     (log/error "Missing configuration parameter " p))))

(defn -main
  [& args]
  (let [cli-options (validate-args args)]
    (cond
      (contains? cli-options :exit-message) (exit (if (:ok? cli-options) 0 1) (:exit-message cli-options))
      :else                                 (start-application (:port cli-options)))))

