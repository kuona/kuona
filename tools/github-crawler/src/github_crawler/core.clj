(ns github-crawler.core
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [kuona-core.util :as util])
  (:gen-class))


(def cli-options
  [["-c" "--config PATH" "Read runtime options from configuration. CLI options override" :default "properties.edn"]
   ["-a" "--api-url URL" "The URL of the back end Kuona API" :default "http://dashboard.kuona.io"]
   ["-f" "--force" "Force updates. Ignore current page position and restart crawling from the first page for all lanaguages" :default false]
   ["-w" "--workspace PATH" "workspace folder for git clones and source operations" :default "/Volumes/data-drive/workspace"]
   ["-i" "--client-id ID" "GitHub client ID to be used to raise the rate limit for collection"]
   ["-s" "--client-secret SECRET" "GitHub client ID secret to be used"]
   ["-p" "--page-file PATH" "File used to track pages during collection to allow for restarts" :default "page.edn"]
   ["-l" "--languages LANG" "A comma separated list of programming language names to be used to search" :default [] :parse-fn #(string/split % #",")]
   ["-h" "--help" "Display this message and exit"]])

(defn usage
  [options-summary]
  (->> ["Kuona GitHub project crawler."
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

(defn load-config [filename]
  (if (util/file-exists? filename)
    (do
      (log/info (str "Reading configuration file \"" filename "\""))
      (with-open [r (clojure.java.io/reader filename)]
        (clojure.edn/read (java.io.PushbackReader. r))))
    (do
      (log/warn (str "Configuration file \"" filename "\" not found"))
      {})))

(defn options-to-configuration
  [options]
  {})

(defn configure
  [args]
  (let [options (validate-args args)]
    (cond
      (contains? options :exit-message) (exit (if (:ok? options) 0 1) (:exit-message options))
      :else
      (merge options (load-config (-> options :config)) options))))

(defn -main
  [& args]
  (configure args))
