(ns snapshot-collector.core
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [kuona-core.cli :as cli]
            [snapshot-collector.snapshot :as snapshot])
  (:gen-class))

(def cli-options
  [["-a" "--api-url URL" "The URL of the back end Kuona API" :default "http://dashboard.kuona.io"]
   ["-f" "--force" "Force updates. Ignore collected snapshots and re-create. Use when additional data is collected as part of a snapshot" :default false]
   ["-w" "--workspace PATH" "workspace folder for git clones and source operations" :default "/Volumes/data-drive/workspace"]
   ["-h" "--help" "Display this message and exit"]])

(defn -main
  [& args]
  (log/info "Kuona Snapshot Collector")
  (let [options (cli/configure "Kuona Snapshot collector." cli-options args)]
    (snapshot/run options)))
