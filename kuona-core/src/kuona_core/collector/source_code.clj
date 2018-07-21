(ns kuona-core.collector.source-code
  (:require [clj-jgit.porcelain :as git]
            [kuona-core.util :as util]))

(defn collect-file [file-path store template]
  (println file-path))

(defn collect
  [store path url repository-id]
  (let [repo (git/load-repo path)]
    (git/git-checkout repo "master")
    (git/git-checkout repo "HEAD"))

  (let [project-path (util/canonical-path path)]
    (doseq [file-path (util/find-files project-path)]
      (collect-file file-path store {:url           url
                                     :repository_id repository-id})
      )))
