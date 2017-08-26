(ns snapshot-collector.core
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as git-query]
            [clojure.java.shell :as shell]
            [cheshire.core :refer :all]
            [slingshot.slingshot :refer :all]
            [kuona-core.metric.store :as store]
            [kuona-core.git :refer :all]
            [kuona-core.util :refer :all]
            [kuona-core.cloc :as cloc]
            [kuona-core.builder :as builder]
            [clj-http.client :as http])
  (:import (java.net InetAddress))
  (:gen-class))

(def cli-options
  [["-a" "--api-url URL" "The URL of the back end Kuona API" :default "http://dashboard.kuona.io"]
   ["-f" "--force" "Force updates. Ignore collected snapshots and re-create. Use when additional data is collected as part of a snapshot" :default false]
   ["-w" "--workspace PATH" "workspace folder for git clones and source operations" :default "/Volumes/data-drive/workspace"]
   ["-h" "--help" "Display this message and exit"]])

(defn usage
  [options-summary]
  (->> ["Kuona Snapshot collector."
        ""
        "Usage: snapshot-collector  [options] action"
        ""
        "Options:"
        options-summary
        ""]
       (string/join \newline)))


(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) {:exit-message (usage summary) :ok? true}
      errors  {:exit-message (error-msg errors)}
      :else options)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn load-config
  [config-stream]
  (let [parsed (parse-stream config-stream true)]
    {:workspace   (:workspace parsed)
     :collections (into [] (:git parsed))}))

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
   :owner_avatar_url  (:owner :avatar_url)
   })

(defn create-snapshot
  [project loc-data builder-data]
  {:repository (project-metrics project)
   :content    loc-data
   :build      builder-data})

(defn put-snapshot
  [snapshot id]
  (let [url (string/join "/" ["http://dashboard.kuona.io/api/snapshots" id])]
    (log/info "put-snapshot " url)
    (parse-json-body (http/put url
                               {:headers {"content-type" "application/json; charset=UTF-8"}
                                :body    (generate-string snapshot)}))))

(defn put-commit!
  [repo-id commit]
  (let [url (string/join "/" ["http://dashboard.kuona.io/api/repositories" repo-id "commits"])]
    (log/info "put-commit " url)
    (parse-json-body (http/put url
                               {:headers {"content-type" "application/json; charset=UTF-8"}
                                :body    (generate-string commit)}))))

(defn has-snapshot?
  [id]
  (let [url (string/join "/" ["http://dashboard.kuona.io/api/snapshots" id])]
    (try+
      (log/info "has-snapshot? " url)
      (http/get url)
      true
      (catch Object _
        false))))

(defn requires-snapshot?
  [id]
  (not (has-snapshot? id)))

(defn language-x-count
  [item k]
  ;  (log/info "language-x-count " item k)
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

(defn snapshot-repository
  [repo]
  (try+
    (let [id (repository-id repo)
          url (repository-git-url repo)
          local-dir (canonical-path (string/join "/" ["/Volumes/data-drive/workspace" id]))
          name (-> repo :project :name)]
      (log/info "Snapshotting " id name "from " url "to " local-dir)
      (if (directory? local-dir) (git-pull url local-dir) (git-clone url local-dir))
      (let [loc-data (cloc/loc-collector (fn [a] a) local-dir "foo")
            build-data (builder/collect-builder-metrics local-dir)
            snapshot-data (create-snapshot (-> repo :project) (loc-metrics loc-data) build-data)]
        (doall (map #(put-commit! id %) (commit-history (git/load-repo local-dir))))
        (log/info "snapshot " (put-snapshot snapshot-data id))
        ))
    (catch Object _
      (log/error (:throwable &throw-context) "Unexpected error"))))

(defn requires-snapshot-filter
  [repo]
  (let [id (repository-id repo)]
    (requires-snapshot? id)))

(defn all-repositories
  ([uri] (all-repositories uri 1))
  ([uri page]
   (let [result (get-repositories (str "http://dashboard.kuona.io/api/repositories?page=" page))
         count (-> result :count)
         items (-> result :items)]
     (if (= count 0) items (concat items (all-repositories uri (inc page)))))))


(defn snapshot
  [optons]
  (let [repositories (all-repositories "http://dashboard.kuona.io/api/repositories")]
    (log/info "Found " (count repositories) " configured repositories for analysis")
    (doall (map snapshot-repository (filter (fn [r] true) repositories)))))

(defn -main
  [& args]
  (log/info "Kuona Snapshot Collector")
  (let [options (validate-args args)]
    (cond
      (contains? options :exit-message) (exit (if (:ok? options) 0 1) (:exit-message options))
      :else (snapshot options)
      )))
;    (doall (map snapshot-repository (filter requires-snapshot-filter repositories)))))
