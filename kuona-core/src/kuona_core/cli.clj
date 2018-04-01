(ns kuona-core.cli
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [kuona-core.util :as util])
  (:gen-class))

(defn usage
  [app-name options-summary]
  (->> [app-name
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
  [app-name cli-options args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) {:exit-message (usage app-name summary) :ok? true}
      errors {:exit-message (error-msg errors)}
      :else options)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn exit-code
  [options]
  (if (:ok? options) 0 1))

(defn configure
  [app-name cli-options args]
  (let [options (validate-args app-name cli-options args)]
    (cond
      (contains? options :exit-message) (exit (exit-code options) (:exit-message options))
      (contains? options :config) (merge options (util/load-config (-> options :config)) options)
      :else options)))
