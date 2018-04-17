(ns git-collector.core
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [cheshire.core :refer :all]
            [kuona-core.store :as store]
            [kuona-core.git :refer :all]
            [kuona-core.util :as util]
            [kuona-core.stores :as stores])
  (:gen-class))

(def cli-options
  [["-c" "--config FILE" "Configuration file JSON format" :default "properties.json"]
   ["-h" "--help"]])

(defn usage
  [options-summary]
  (->> ["Kuona Git data collector."
        ""
        "Usage: git-collector  [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn load-config
  [config-stream]
  (let [parsed (parse-stream config-stream true)]
    {:workspace   (:workspace parsed)
     :collections (into [] (:git parsed))}))

(defn -main
  [& args]
  (log/info "Kuona Git Collector")

  (let [options (parse-opts args cli-options)
        config-file (:config (:options options))
        config (load-config (util/file-reader config-file))
        repositories (store/all-documents stores/repositories-store)
        urls (map #(-> % :url) repositories)]
    (log/info "Found " (count repositories) " configured repositories for analysis")
    (stores/create-stores)

    (doseq [url urls] (collect stores/commit-logs-store stores/code-metric-store "/Volumes/data-drive/workspace" url))))
