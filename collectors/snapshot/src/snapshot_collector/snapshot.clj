(ns snapshot-collector.snapshot
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clj-jgit.porcelain :as git]
            [cheshire.core :refer :all]
            [slingshot.slingshot :refer :all]
            [clj-http.client :as http]
            [kuona-core.metric.store :as store]
            [kuona-core.git :refer :all]
            [kuona-core.util :refer :all]
            [kuona-core.cloc :as cloc]
            [kuona-core.builder :as builder])
  (:gen-class))

(defn get-repositories
  [url]
  (parse-json-body (http/get url)))

(defn repository-id
  [r]
  (:id r))

(defn repository-git-url
  [r]
  (-> r :url))

(defn project-metrics
  [project]
  {:name              (:name project)
   :description       (:description project)
   :open_issues_count (:open_issues_count project)
   :watchers_count    (:watchers_count project)
   :forks_count       (:forks_count project)
   :stargazers_count  (:stargazers_count project)
   :size              (:size project)
   :updated_at        (:updated_at project)
   :language          (:language project)
   :pushed_at         (:pushed_at project)
   :owner_avatar_url  (:avatar_url (:owner project))
   })

(defn create-snapshot
  [project loc-data builder-data]
  {:repository (project-metrics project)
   :content    loc-data
   :build      builder-data})

(defn snapshot-url
  [api-url id]
  (string/join "/" [api-url "api/snapshots" id]))

(defn build-json-request
  [content]
  {:headers {"content-type" "application/json; charset=UTF-8"}
   :body    (generate-string content)})

(defn put-snapshot!
  [snapshot url]
  (log/info "put-snapshot " url)
  (let [request (build-json-request snapshot)]
    (parse-json-body (http/put url request))))

(defn snapshot-commits-url
  [api-url id]
  (string/join "/" [(snapshot-url api-url id) "commits"]))

(defn put-commit!
  [commit url]
  (log/info "put-commit " url)
  (let [request (build-json-request commit)]
    (parse-json-body (http/put url request))))

(defn has-snapshot?
  [id api-url]
  (let [url (string/join "/" [api-url "api/snapshots" id])]
    (try+
      (log/info "has-snapshot? " url)
      (http/get url)
      true
      (catch Object _
        false))))

(defn language-x-count
  [item k]
  {:language (:language item) :count (k item)})

(defn loc-metrics
  [loc-data]
  (let [content (-> loc-data :metric :activity)]
    {:file_count           (:file-count content)
     :comment_lines        (:comment-lines content)
     :blank_lines          (:blank-lines content)
     :code_lines           (:code-lines content)
     :file_details         (into [] (map #(language-x-count % :file-count) (:languages content)))
     :blank_line_details   (into [] (map #(language-x-count % :blank-lines) (:languages content)))
     :comment_line_details (into [] (map #(language-x-count % :comment-lines) (:languages content)))
     :code_line_details    (into [] (map #(language-x-count % :code-lines) (:languages content)))
     }))

(defn- clone-or-update
  [url local-dir]
  (if (directory? local-dir) (git-pull url local-dir) (git-clone url local-dir)))

(defn create-repository-snapshot
  [api-url workspace repo]
  (try+
   (let [id        (repository-id repo)
         url       (repository-git-url repo)
         local-dir (canonical-path (string/join "/" [workspace id]))
         name      (-> repo :project :name)]
      (log/info "Creating repository snapshot " id name "from " url "to " local-dir)
      (clone-or-update url local-dir)
      (let [loc-data      (cloc/loc-collector (fn [a] a) local-dir "foo")
            build-data    (builder/collect-builder-metrics local-dir)
            snapshot-data (create-snapshot (-> repo :project) (loc-metrics loc-data) build-data)]
        (doall (map #(put-commit! % (snapshot-commits-url api-url id)) (commit-history (git/load-repo local-dir))))
        (log/info "snapshot " (put-snapshot! snapshot-data (snapshot-url api-url id)))))
    (catch Object _
      (log/error (:throwable &throw-context) "Unexpected error"))))

(defn requires-snapshot?
  [repo api-url]
  (let [id (repository-id repo)]
    (not (has-snapshot? id api-url))))

(defn all-repositories
  ([api-uri] (all-repositories api-uri 1))
  ([api-uri page]
   (do
     (log/info "Reading repository page " page)
     (let [result (get-repositories (str api-uri "/api/repositories?page=" page))
           count  (-> result :count)
           items  (-> result :items)]
       (if (= count 0) items (concat items (all-repositories api-uri (inc page))))))))

(defn- create-snapshots
  [api-url workspace repositories]
  (log/info "Creating snapshots for " (count repositories) " repositories")
  (doall (map (fn [repo] (create-repository-snapshot api-url workspace repo)) repositories)))

(defn run
  "Main entry point for snapshot collection."
  [options]
  (let [api-url         (:api-url options)
        workspace       (:workspace options)
        force-update    (:force options)
        requires-update (fn [r] (if force-update true (requires-snapshot? r api-url)))]
    (log/info "Updating " api-url " using " workspace " for repository data")
    (let [repositories (all-repositories api-url)]
      (log/info "Found " (count repositories) " configured repositories for analysis")
      (create-snapshots api-url workspace (filter requires-update repositories)))))
