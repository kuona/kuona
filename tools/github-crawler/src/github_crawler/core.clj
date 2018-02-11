(ns github-crawler.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [kuona-core.cli :as cli]
            [github-crawler.crawler :as crawler])
  (:gen-class))


(def cli-options
  [["-c" "--config PATH" "Read runtime options from configuration. CLI options override" :default "properties.edn"]
   ["-a" "--api-url URL" "The URL of the back end Kuona API" :default "http://dashboard.kuona.io"]
   ["-f" "--force" "Force updates. Ignore current page position and restart crawling from the first page for all lanaguages" :default false]
   ["-w" "--workspace PATH" "workspace folder for git clones and source operations" :default "/Volumes/data-drive/workspace"]
   ["-i" "--client-id ID" "GitHub client ID to be used to raise the rate limit for collection"]
   ["-s" "--client-secret SECRET" "GitHub client ID secret to be used"]
   ["-p" "--page-file PATH" "File used to track pages during collection to allow for restarts" :default "page.edn"]
   ["-l" "--languages LANG" "A comma separated list of programming language names to be used to search" :parse-fn #(string/split % #",")]
   ["-h" "--help" "Display this message and exit"]])

(defn -main
  [& args]
  (crawler/crawl (cli/configure "Kuona GitHub Crawler" cli-options args)))
