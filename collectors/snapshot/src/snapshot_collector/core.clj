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
  [["-c" "--config FILE" "Configuration file JSON format" :default "properties.json"]
   ["-h" "--help"]])

(defn usage
  [options-summary]
  (->> ["Kuona Snapshot collector."
        ""
        "Usage: snapshot-collector  [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

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
   :build builder-data})

(defn put-snapshot
  [snapshot id]
  (let [url (string/join "/" ["http://dashboard.kuona.io/api/snapshots" id])]
    (log/info "put-snapshot " url)
    (parse-json-body (http/put url
                               {:headers {"content-type" "application/json; charset=UTF-8"}
                                :body    (generate-string snapshot)}))))

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
   (let [id        (repository-id repo)
         url       (repository-git-url repo)
         local-dir (canonical-path (string/join "/" ["/Volumes/data-drive/workspace" id]))
         name      (-> repo :project :name)]
     (log/info "Snapshotting " id name "from " url "to " local-dir)
     (if (directory? local-dir) (git-pull url local-dir) (git-clone url local-dir))
     (let [loc-data      (cloc/loc-collector (fn [a] a) local-dir "foo")
           build-data    (builder/collect-builder-metrics local-dir)
           snapshot-data (create-snapshot (-> repo :project) (loc-metrics loc-data) build-data)]
       (log/info "snapshot " (put-snapshot snapshot-data id))))
   (catch Object _
     (log/error (:throwable &throw-context) "Unexpected error"))))



(defn all-repositories
  ([uri] (all-repositories uri 1))
  ([uri page]
   (let [result (get-repositories (str "http://dashboard.kuona.io/api/repositories?page=" page))
         count  (-> result :count)
         items  (-> result :items)]
    (if (= count 0) items (concat items (all-repositories uri (inc page)))))))


(defn -main
  [& args]
  (log/info "Kuona Snapshot Collector")
  
  (let [options          (parse-opts args cli-options)
        repositories     (all-repositories "http://dashboard.kuona.io/api/repositories")]
    (log/info "Found " (count repositories) " configured repositories for analysis")
    (doall (map snapshot-repository repositories))))
