(ns tfs-crawler.crawler
  (:require [clojure.tools.logging :as log]
            [cheshire.core :refer :all]
            [slingshot.slingshot :refer :all]
            [kuona-api.core.util :as util]
            [kuona-api.core.collector.tfs :as tfs])
  (:gen-class))


(defn put-repository
  [entry api-url]
  (let [id  (util/uuid-from (-> entry :url))
        url (clojure.string/join "/" [api-url "/api/repositories" id])]
    (log/info "put-repository " (-> entry :url) "to" url)
    (kuona-api.core.http/json-put url entry)))

(defn crawl
  [config]
  (let [token   (-> config :token)
        api-url (-> config :api-url)
        org     (-> config :org)]
    (log/info "TFS user token       " token)
    (log/info "Publish to           " api-url)
    (log/info "Organisation         " org)

    (let [entries (tfs/find-organization-repositories org token)]
      (doseq [entry entries] (put-repository entry api-url)))))
