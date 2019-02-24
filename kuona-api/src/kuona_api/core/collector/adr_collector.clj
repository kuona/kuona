(ns kuona-api.core.collector.adr-collector
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [kuona-api.core.util :as util])
  (:import (java.io File)))



(defn directory [^String path]
  (let [adr-dir (io/file path ".adr-dir")]
    (with-open [rdr (io/reader (.getCanonicalPath adr-dir))]
      (first (line-seq rdr)))))

(defn file-type [^String path]
  (cond
    (re-matches #".*\.md" path) :markdown
    (re-matches #".*\.adoc" path) :asciidoc
    :else :text))

(defn read-decision [^String base ^File path]
  (let [canonical-path (.getCanonicalPath path)
        file           (io/file canonical-path)]
    {:file     {:path (string/replace-first canonical-path base "")
                :name (.getName file)
                :type (file-type canonical-path)}
     :contents (slurp file)}
    )
  )

(defn decisions [^String base ^String path]
  (let [canonical-base (util/canonical-path-from-string base)
        path-file      (io/file base path)]
    {:adrs (->> path-file
                io/file
                file-seq
                (filter #(.isFile %))
                (mapv #(read-decision canonical-base %)))}
    ))
