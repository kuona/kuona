(ns kuona-api.core.collector.readme
  (:require [clojure.java.io :as io]))

(defn read-readme-file
  [path]
  (cond
    (.exists (io/file path "README.md")) (io/file path "README.md")
    (.exists (io/file path ".github/README.md")) (io/file path ".github/README.md")
    (.exists (io/file path "docs/README.md")) (io/file path "docs/README.md")
    :else nil))

(defn collect
  "Collects the repository readme file data"
  [localpath]
  (let [readme-file (read-readme-file localpath)]
    (cond
      readme-file {:readme {:found true
                            :text  (slurp readme-file)}}
      :else {:readme {:found false
                      :text  ""}})))
