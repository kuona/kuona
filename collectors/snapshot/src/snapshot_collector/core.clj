(ns snapshot-collector.core
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [snapshot-collector.snapshot :as snapshot])
  (:gen-class))

(def cli-options
  [["-a" "--api-url URL" "The URL of the back end Kuona API" :default "http://dashboard.kuona.io"]
   ["-f" "--force" "Force updates. Ignore collected snapshots and re-create. Use when additional data is collected as part of a snapshot" :default false]
   ["-w" "--workspace PATH" "workspace folder for git clones and source operations" :default "/Volumes/data-drive/workspace"]
   ["-h" "--help" "Display this message and exit"]])

(defn usage
  [options-summary]
  (->> ["Kuona Snapshot collector."
        ""
        "Usage: snapshot-collector  [options] action"
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

(defn -main
  [& args]
  (log/info "Kuona Snapshot Collector")
  (let [options (validate-args args)]
    (cond
      (contains? options :exit-message) (exit (if (:ok? options) 0 1) (:exit-message options))
      :else (snapshot/run options))))
