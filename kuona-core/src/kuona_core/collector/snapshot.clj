(ns kuona-core.collector.snapshot
  (:require
    [clojure.tools.logging :as log]
    [cheshire.core :refer :all]
    [slingshot.slingshot :refer :all]
    [kuona-core.store :as store]
    [kuona-core.stores :as stores]
    [kuona-core.git :refer :all]
    [kuona-core.cloc :as cloc]
    [kuona-core.builder :as builder])
  (:gen-class))


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


(defn create-repository-snapshot
  [local-dir repo]
  (let [id   (-> repo :id)
        url  (-> repo :url)
        name (-> repo :name)]
    (try+
      (log/info "Creating repository snapshot id" id "name" name "from " url "to " local-dir)
      (git-pull url local-dir)
      (let [loc-data      (cloc/loc-collector (fn [a] a) local-dir "foo")
            build-data    (builder/collect-builder-metrics local-dir)
            snapshot-data (create-snapshot (-> repo :project) (loc-metrics loc-data) build-data)]

        (store/put-document snapshot-data stores/snapshots-store id))
      (catch Object _
        (log/error (:throwable &throw-context) "Unexpected error creating snapshot for" name "id" id "url" url)))))








