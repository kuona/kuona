(ns tfs-crawler.crawler
  (:require [clojure.tools.logging :as log]
            [clj-http.client :as http]
            [cheshire.core :refer :all]
            [slingshot.slingshot :refer :all]
            [kuona-core.util :as util])
  (:gen-class))

(defn vs-url [org]
  (str "https://" org ".visualstudio.com/_apis/git/repositories?api-version=4.1"))


(defn put-repository
  [entry api-url]
  (let [id (util/uuid-from (-> entry :url))
        url (clojure.string/join "/" [api-url "/api/repositories" id])]
    (log/info "put-repository " (-> entry :url) "to" url)
    (util/parse-json-body (http/put url {:headers {"content-type" "application/json; charset=UTF-8"}
                                         :body    (generate-string entry)}))))
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

(defn crawl
  [config]
  (let [token (-> config :token)
        api-url (-> config :api-url)
        org (-> config :org)]
    (log/info "TFS user token       " token)
    (log/info "Publish to           " api-url)
    (log/info "Organisation         " org)

    (let [vs-response (read-available-repositories (vs-url (-> config :org)) token)
          items (-> vs-response :value)
          entries (map #(tfs-to-repository-entry %) items)]
      (doseq [entry entries] (put-repository entry api-url)))))
