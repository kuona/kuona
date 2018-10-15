(ns tfs-crawler.core
  (:require [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [kuona-api.core.cli :as cli]
            [tfs-crawler.crawler :as crawler]
            )
  (:gen-class))


(def tfs-cli-options
  [["-c" "--config PATH" "Read runtime options from configuration. CLI options override" :default "properties.edn"]
   ["-a" "--api-url URL" "The URL of the back end Kuona API" :default "http://dashboard.kuona.io"]
   ["-t" "--token TOKEN" "TFS client ID to be used to raise the rate limit for collection"]
   ["-o" "--org ID" "TFS organisation id"]
   ["-h" "--help" "Display this message and exit"]])

(defn -main
  [& args]
  (let [config (cli/configure "Kuona TFS Crawler" tfs-cli-options args)]
    (crawler/crawl config)))
