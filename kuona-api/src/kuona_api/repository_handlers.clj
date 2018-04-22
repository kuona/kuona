(ns kuona-api.repository-handlers
  (:require [cheshire.core :as json]
            [slingshot.slingshot :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.store :as store]
            [ring.util.response :refer [resource-response response status]]
            [kuona-core.util :as util]
            [kuona-core.github :as github]
            [kuona-core.stores :refer [repositories-store commit-logs-store]])
  (:gen-class))

(defn bad-request
  [error]
  {:status  400
   :headers {"Content-Type" "application-json"}
   :body    (json/generate-string {:error error})})

(defn repository-page-link
  [page-number]
  (str "/api/repositories?page=" page-number))

(defn get-repository-count [] (response (store/get-count repositories-store)))

(defn get-repositories
  [search page]
  (log/info "get repositories" search page)
  (response (store/search repositories-store search 1000 page repository-page-link)))

(defn commits-page-link
  [id page-number]
  (str "/api/repositories/" id "/commits?page=" page-number))

(defn get-commits
  [id page]
  (log/info "finding commits for repository " id " page " page)
  (response (store/search commit-logs-store (str "repository_id:" id) 100 page #(commits-page-link id %))))

(defn get-repository-by-id
  [id]
  (response (:_source (store/get-document repositories-store id))))

(defn put-repository!
  ([repo] (put-repository! repo (:id repo)))
  ([repo id]
   (response (store/put-document repo repositories-store id))))

(defn put-commit!
  [id commit]
  (log/info "new commit for repository " id)
  (let [commit-id (-> commit :id)
        entity    (merge commit {:repository_id id})]
    (cond
      (nil? commit-id) (bad-request "malformed request - missing commit identity")
      :else (response (store/put-document entity commit-logs-store commit-id)))))


(defn github-to-repository-record
  [github-repo]
  {:source            :github
   :name              (-> github-repo :name)
   :url               (-> github-repo :git_url)
   :git_url           (-> github-repo :git_url)
   :description       (-> github-repo :description)
   :avatar_url        (-> github-repo :owner :avatar_url)
   :project_url       (-> github-repo :html_url)
   :created_at        (-> github-repo :created_at)
   :updated_at        (-> github-repo :updated_at)
   :open_issues_count (-> github-repo :open_issues_count)
   :watchers          (-> github-repo :watchers)
   :forks             (-> github-repo :forks)
   :size              (-> github-repo :size)
   :last_analysed     nil
   :project           github-repo})

(defn test-project-url
  [project]
  (log/info "test-project-url" project)

  (let [username   (-> project :username)
        repository (-> project :repository)]

    (cond
      (and username repository) (response (github/get-project-repository username repository))
      username (response {:status :test-repos})
      :else (response {:status :invalid-parameters}))))

;(cond
;  (== (-> project :source) :github) (test-github-project))

(def invalid-add-repository-request
  {:status      :error
   :description "Request not recognised"
   :message     "Supported requests github-project"})

(defn foo [resp fn]
  (cond
    (= (:status resp) :success) (fn resp)
    :else resp)
  )

(defn add-id
  [r selector]
  (cond
    (-> r selector) (merge r {:id (util/uuid-from (-> r selector))})
    :else (merge r {:id (util/uuid)})
    )
  )

(defn github-project-request?
  [spec]
  (= (:source spec) "github-project"))

(defn handle-github-project-request
  [spec]
  (-> spec
      (github/get-project-repository)
      (:github)
      (github-to-repository-record)
      (add-id :git_url)
      (put-repository!)))

(defn add-repository
  "Handles a request to add a new repository based on the supplied spec

  :source

   github-project - add a single project"

  [spec]
  (log/info "add-repository" spec)
  (response
    (cond
      (github-project-request? spec) (handle-github-project-request spec)
      :else invalid-add-repository-request)))
