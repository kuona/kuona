(ns kuona-core.util
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all])
  (:gen-class))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn uuid-from
  ([a b] (uuid-from (str a b)))
  ([s] (str (java.util.UUID/nameUUIDFromBytes (.getBytes s)))))

(defn not-nil? [v] (not (nil? v)))

(defn directory?
  "Tests the path for being a directory"
  [path]
  (.isDirectory (io/as-file path)))

(defn timestamp
  "Generate a timestamp for the current instant"
  []
  (new java.util.Date))

(defn canonical-path 
  "Returns canonical path of a given path"
  [path] 
  (.getCanonicalPath (io/file path)))

(defn file-reader
  [path]
  (clojure.java.io/reader path))

(defn find-files
  [path]
  (file-seq (clojure.java.io/file path)))

(defn parse-json-body
  [response]
  (parse-string (:body response) true))
