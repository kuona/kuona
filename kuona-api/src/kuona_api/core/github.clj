(ns kuona-api.core.github
  (:require [slingshot.slingshot :refer :all]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [kuona-api.core.http :as http]
            [kuona-api.core.http :as http-util]))

(defn get-project-repository
  "Read repository details from github"
  ([spec] (get-project-repository (:username spec) (:repository spec)))
  ([username repository]
   (http-util/wrap-http-call
     #(let [url (string/join "/" ["https://api.github.com/repos" username repository])]
        {:status :success
         :github (http/json-get url)}))))

(defn get-project-repositories
  [project-name]
  (http-util/wrap-http-call
    #(let [url (string/join "/" ["https://api.github.com/users" project-name "repos"])]
       (log/info "github-query" url)
       {:status :success
        :github (http/json-get url)})))
