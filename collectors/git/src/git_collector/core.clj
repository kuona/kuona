(ns git-collector.core
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as git-query]
            [clojure.java.shell :as shell]
            [cheshire.core :refer :all]
            [kuona-core.metric.store :as store]
            [kuona-core.git :refer :all]
            [kuona-core.util :refer :all]
            [kuona-core.cloc :as cloc])
  (:import (java.net InetAddress))
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
  
  (let [options          (parse-opts args cli-options)
        config-file      (:config (:options options))
        config           (load-config (file-reader config-file))
        index            (store/index :kuona-data "http://localhost:9200")
        vcs-mapping      (store/mapping :vcs index)
        code-mapping     (store/mapping :code index)
        repositories-url (store/mapping :repositories index)
        repositories     (store/all-documents repositories-url)
        urls             (map #(-> % :url) repositories)]
    (log/info "Found " (count repositories) " configured repositories for analysis")
    (if (not (store/has-index? index)) (store/create-index index store/metric-mapping-type))
    (doseq [url urls] (collect vcs-mapping code-mapping "/Volumes/data-drive/workspace" url))))
