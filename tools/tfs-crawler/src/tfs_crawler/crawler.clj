(ns tfs-crawler.crawler
  (:require [clojure.tools.logging :as log]
            [clj-http.client :as http]
            [cheshire.core :refer :all]
            [slingshot.slingshot :refer :all]
            [kuona-core.util :as util])
  (:gen-class))

(defn vs-url [org]
  (str "https://" org ".visualstudio.com/_apis/git/repositories?api-version=4.1"))


(defn tfs-to-repository-entry
  [item]
  {:source          :tfs
   :github_language nil
   :url             (-> item :sshUrl)
   :project         item
   :last_analysed   nil})

(defn read-available-repositories
  [url, token]
  (util/parse-json-body (http/get url {:basic-auth ["" token]})))

(defn crawl
  [config]
  (log/info "TFS user token       " (-> config :token))
  (log/info "Publish to           " (-> config :api-url))
  (log/info "For languages        " (-> config :languages)))
