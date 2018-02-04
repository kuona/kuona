(ns kuona-api.repository-handlers
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [slingshot.slingshot :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.metric.store :as store]
            [ring.util.response :refer [resource-response response status]]
            [clojure.string :as string]
            [kuona-core.util :as util]
            [kuona-core.github :as github])
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


(defn github-to-repository-record
  [github-repo]

  {:source            :github
   :name              (-> github-repo :project :name)
   :description       (-> github-repo :project :description)
   :avatar_url        (-> github-repo :project :owner :avatar_url)
   :project_url       (-> github-repo :project :html_url)
   :created_at        (-> github-repo :project :created_at)
   :updated_at        (-> github-repo :project :updated_at)
   :open_issues_count (-> github-repo :project :open_issues_count)
   :watchers          (-> github-repo :project :watchers)
   :forks             (-> github-repo :project :forks)
   :size              (-> github-repo :project :size)
   :last_analysed     nil
   :github            github-repo}
  )

(defn test-project-url
  [project]
  (log/info "test-project-url" project)

  (let [username (-> project :username)
        repository (-> project :repository)]

    (cond
      (and username repository) (response (github/get-project-repository username repository))
      username (response {:status :test-repos})
      :else (response {:status :invalid-parameters}))))

;(cond
;  (== (-> project :source) :github) (test-github-project))
