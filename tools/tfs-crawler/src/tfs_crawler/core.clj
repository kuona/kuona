(ns tfs-crawler.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [kuona-core.cli :as cli]
            [tfs-crawler.crawler :as crawler])
  (:gen-class))


(def cli-options
  [["-c" "--config PATH" "Read runtime options from configuration. CLI options override" :default "properties.edn"]
   ["-a" "--api-url URL" "The URL of the back end Kuona API" :default "http://dashboard.kuona.io"]
   ["-t" "--token" "TFS client ID to be used to raise the rate limit for collection"]
   ["-o" "--org" "TFS organisation id"]
   ["-h" "--help" "Display this message and exit"]])

(defn -main
  [& args]
  (crawler/crawl (cli/configure "Kuona TFS Crawler" cli-options args)))
