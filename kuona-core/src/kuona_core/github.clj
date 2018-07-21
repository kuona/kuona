(ns kuona-core.github
  (:require [slingshot.slingshot :refer :all]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [kuona-core.http :as http]
            [kuona-core.util :as util]))


(defn wrap-http-call
  [f]
  (try+
    (f)
    (catch [:status 400] {:keys [request-time headers body]}
      (let [error (util/parse-json body)]
        (log/info "Bad request" error)
        {:status :error}))
    (catch [:status 404] {:keys [request-time headers body]}
      (let [error (util/parse-json body)]
        (log/info "Not authorized" error)
        {:status :error
         :cause  404}))
    (catch Object _
      (log/error "Unexpected exception " (:message &throw-context))
      {:status  :error
       :message (:message &throw-context)
       :cause   (:cause &throw-context)})))


(defn get-project-repository
  "Read repository details from github"
  ([spec] (get-project-repository (:username spec) (:repository spec)))
  ([username repository]
   (wrap-http-call
     #(let [url (string/join "/" ["https://api.github.com/repos" username repository])]
        {:status :success
         :github (http/json-get url)}))))

(defn get-project-repositories
  [project-name]
  (wrap-http-call
    #(let [url (string/join "/" ["https://api.github.com/users" project-name "repos"])]
       (log/info "github-query" url)
       {:status :success
        :github (http/json-get url)})))
