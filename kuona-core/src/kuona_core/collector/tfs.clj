(ns kuona-core.collector.tfs
  (:require [clj-http.client :as http]
            [kuona-core.util :as util]))

(defn vs-url
  [org]
  (str "https://" org ".visualstudio.com/_apis/git/repositories?api-version=4.1"))

(defn tfs-to-repository-entry
  [item]
  {:source          :tfs
   :github_language nil
   :url             (-> item :sshUrl)
   :project         item
   :last_analysed   nil
   :name            (-> item :name)})

(defn read-available-repositories
  [url, token]
  (util/parse-json-body (http/get url {:basic-auth ["" token]})))

(defn find-organization-repositories
  [org token]
  (let [vs-response (read-available-repositories (vs-url org) token)
        items (-> vs-response :value)
        entries (map #(tfs-to-repository-entry %) items)]
    entries))
