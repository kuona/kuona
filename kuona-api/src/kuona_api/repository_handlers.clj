(ns kuona-api.repository-handlers
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [slingshot.slingshot :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.metric.store :as store]
            [ring.util.response :refer [resource-response response status]]
            [clojure.string :as string]
            [kuona-core.util :as util])
  (:gen-class)
  (:import (java.net URL)))

(defn bad-request
  [error]
  {:status  400
   :headers {"Content-Type" "application-json"}
   :body    (json/generate-string {:error error})})



(def repositories (store/mapping :repositories (store/index :kuona-repositories "http://localhost:9200")))
(def commits (store/mapping :commits (store/index :kuona-repositories "http://localhost:9200")))

(defn repository-page-link
  [page-number]
  (str "/api/repositories?page=" page-number))

(defn get-repository-count [] (response (store/get-count repositories)))

(defn get-repositories
  [search page]
  (log/info "get repositories" search page)
  (response (store/search repositories search 100 page repository-page-link)))

(defn commits-page-link
  [id page-number]
  (str "/api/repositories/" id "/commits?page=" page-number))

(defn get-commits
  [id page]
  (log/info "finding commits for repository " id " page " page)
  (response (store/search commits (str "repository_id:" id) 100 page #(commits-page-link id %))))

(defn get-repository-by-id
  [id]
  (response (:_source (store/get-document repositories id))))

(defn put-repository!
  [id repo]
  (response (store/put-document repo repositories id)))

(defn put-commit!
  [id commit]
  (log/info "new commit for repository " id)
  (let [commit-id (-> commit :id)
        entity (merge commit {:repository_id id})]
    (cond
      (nil? commit-id) (bad-request "malformed request - missing commit identity")
      :else (response (store/put-document entity commits commit-id)))))

(defn test-github-project
  [project]
  (log/info "test-github-project" project)
  (let [github-url (-> project :url)
        url (URL. github-url)
        path (.getPath url)
        e (string/split path #"/")]
    (println "test github " url)
    (println path)
    (println e)
    path))

(defn query-github-repo
  [username repository]

  (try+
    (let [url (string/join "/" ["https://api.github.com/repos" username  repository])]
      {:status :success
       :github (util/parse-json-body (http/get url))
       })
    (catch [:status 400] {:keys [request-time headers body]}
      (let [error (util/parse-json body)]
        (log/info "Bad request" error)
        {:status :error}))
    (catch [:status 404] {:keys [request-time headers body]}
      (let [error (util/parse-json body)]
        (log/info "Bad request" error)
        {:status :error}))
    (catch Object _
      (log/error (:throwable &throw-context) "Unexpected error reading schema" username repository)
      {:status :error})))

(defn test-project-url
  [project]
  (log/info "test-project-url" project)

  (let [username (-> project :username)
        repository (-> project :repository)]

    (cond
      (and username repository) (response (query-github-repo username repository))
      username (response {:status :test-repos})
      :else (response {:status :invalid-parameters}))))

;(cond
;  (== (-> project :source) :github) (test-github-project))
