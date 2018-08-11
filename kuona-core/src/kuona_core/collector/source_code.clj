(ns kuona-core.collector.source-code
  (:require [clj-jgit.porcelain :as git]
            [kuona-core.util :as util]
            [clojure.tools.logging :as log])
  (:import (java.io File)
           (java.nio.file Files LinkOption)
           (java.nio.file.attribute BasicFileAttributes)))


(def nofollow-links
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn file-attributes
  [^File file]
  (let [path       (.toPath file)
        attributes (Files/readAttributes path BasicFileAttributes nofollow-links)]
    {:size          (.size attributes)
     :last_modified (.toString (.lastModifiedTime attributes))
     :created       (.toString (.creationTime attributes))}))

(defn collect-source-file
  [file-type file-path relative-path template]
  (merge template
         {:path relative-path
          :type file-type
          :text (slurp file-path)}
         (file-attributes file-path)))

(defn ignore-source-file
  [file-type file-path relative-path template]
  (log/info "ignoring file " relative-path)
  (merge template
         {:path relative-path
          :type file-type}
         (file-attributes file-path)))

(defn collect-file
  [project-path file-path store template]
  (let [relative-path (subs (.getAbsolutePath file-path) (count project-path))]
    ;(println file-path relative-path)
    ;(println relative-path)
    (cond
      (nil? file-path) (fn [p] nil)
      (util/directory? file-path) (fn [p] nil)
      (re-matches #".*\.java" relative-path) (collect-source-file :java file-path relative-path template)
      (re-matches #".*\.clj" relative-path) (collect-source-file :clojure file-path relative-path template)
      (re-matches #".*\.js" relative-path) (collect-source-file :javascript file-path relative-path template)
      (re-matches #".*\.md" relative-path) (collect-source-file :markdown file-path relative-path template)
      (re-matches #".*\.sh" relative-path) (collect-source-file :shell file-path relative-path template)
      (re-matches #".*\.yml" relative-path) (collect-source-file :yaml file-path relative-path template)
      (re-matches #".*\.json" relative-path) (collect-source-file :json file-path relative-path template)
      (re-matches #".*\.html" relative-path) (collect-source-file :html file-path relative-path template)
      (re-matches #".*\.txt" relative-path) (collect-source-file :txt file-path relative-path template)
      :else (ignore-source-file :ignored file-path relative-path template)))
  )

(defn pre-collect
  [path]
  (let [repo (git/load-repo path)]
    (git/git-checkout repo "master")
    (git/git-checkout repo "HEAD"))
  )


(defn collect
  [store path url repository-id]

  ;(pre-collect path)

  (let [project-path (util/canonical-path path)]
    (doseq [file-path (util/find-files project-path)]
      (let [template {:url           url
                      :repository_id repository-id}]
        (collect-file project-path file-path store template)))))
