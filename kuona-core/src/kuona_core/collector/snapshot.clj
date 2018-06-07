(ns kuona-core.collector.snapshot
  (:require
    [clojure.tools.logging :as log]
    [cheshire.core :refer :all]
    [slingshot.slingshot :refer :all]
    [kuona-core.store :as store]
    [kuona-core.stores :as stores]
    [kuona-core.git :refer :all]
    [kuona-core.cloc :as cloc]
    [kuona-core.builder :as builder]
    [kuona-core.workspace :refer [get-workspace-path]]
    [kuona-core.util :as util]
    [kuona-core.collector.manifest :as manifest])
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
    (assert (not (nil? content)) (str "content missing from loc-data " loc-data))
    {:file_count           (:files content)
     :comment_lines        (:comments content)
     :blank_lines          (:blanks content)
     :code_lines           (:code content)
     :file_details         (into [] (map #(language-x-count % :files) (:languages content)))
     :blank_line_details   (into [] (map #(language-x-count % :blanks) (:languages content)))
     :comment_line_details (into [] (map #(language-x-count % :comments) (:languages content)))
     :code_line_details    (into [] (map #(language-x-count % :code) (:languages content)))
     }))


(defn create-repository-snapshot
  [local-dir repo]
  (let [id   (-> repo :id)
        url  (-> repo :url)
        name (-> repo :name)]
    (try+
      (log/info "Creating repository snapshot id" id "name" name "from " url "to " local-dir)
      (if (util/directory? local-dir)
        (git-pull url local-dir)
        (git-clone url local-dir))
      (let [loc-data      (cloc/loc-collector local-dir)
            build-data    (builder/collect-builder-metrics local-dir)
            snapshot-data (create-snapshot (-> repo :project) (loc-metrics loc-data) build-data)
            manifest      (manifest/collect local-dir)]

        (store/put-document (merge snapshot-data manifest) stores/snapshots-store id))
      (catch Object _
        (log/error (:throwable &throw-context) "Unexpected error creating snapshot for" name "id" id "url" url)))))
