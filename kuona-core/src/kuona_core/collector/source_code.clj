(ns kuona-core.collector.source-code
  (:require [clj-jgit.porcelain :as git]
            [kuona-core.util :as util])
  (:import (java.io File)
           (java.nio.file Files LinkOption Path)
           (java.nio.file.attribute BasicFileAttributes)))


(def nofollow-links
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn file-attributes
  [^File file]
  (println file)
  (let [path       (.toPath file)
        attributes (Files/readAttributes path BasicFileAttributes nofollow-links)]
    {:size          (Files/size ^Path path)
     :last_modified (Files/getLastModifiedTime path nofollow-links)
     :attributes    attributes}
    ))

(defn collect-file
  [project-path file-path store template]
  (let [relative-path (subs (.getAbsolutePath file-path) (count project-path))]
    ;(println file-path relative-path)
    ;(println relative-path)
    (cond
      (nil? file-path) (fn [p] nil)
      (re-matches #".*\.java" relative-path) (println "Java file " (merge template
                                                                          {:path relative-path
                                                                           :type :java
                                                                           :text (slurp file-path)}
                                                                          (file-attributes file-path)))
      :else nil))
  )

(defn collect
  [store path url repository-id]
  (let [repo (git/load-repo path)]
    (git/git-checkout repo "master")
    (git/git-checkout repo "HEAD"))

  (let [project-path (util/canonical-path path)]
    (doseq [file-path (util/find-files project-path)]
      (collect-file project-path file-path store {:url           url
                                                  :repository_id repository-id}))))
