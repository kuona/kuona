(ns kuona-core.util
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all])
  (:import java.util.Properties)
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

(defn file-exists?
  [file-path]
  (.exists (io/file file-path)))

(defn timestamp
  "Generate a timestamp for the current instant"
  []
  (new java.util.Date))

(defn canonical-path
  "Returns canonical path of a given path"
  [path]
  (.getCanonicalPath (io/file path)))

(defn absolute-path
  [path]
  (.getAbsolutePath (io/file path)))

(defn file-reader
  [path]
  (clojure.java.io/reader path))

(defn find-files
  [path]
  (file-seq (clojure.java.io/file path)))

(defn parse-json
  [text]
  (parse-string text true))

(defn parse-json-body
  [response]
  (parse-json (:body response)))

(defn get-project-version
  "Reads the project version for the supplied dependency
  
  usage (get-project-version 'projectname)"
  [dep]
  (let [path (str "META-INF/maven/" (or (namespace dep) (name dep))
                  "/" (name dep) "/pom.properties")
        props (io/resource path)]
    (when props
      (with-open [stream (io/input-stream props)]
        (let [props (doto (Properties.) (.load stream))]
          (.getProperty props "version"))))))

(defn load-config [filename]
  (if (file-exists? filename)
    (do
      (log/info (str "Reading configuration file \"" filename "\""))
      (with-open [r (clojure.java.io/reader filename)]
        (clojure.edn/read (java.io.PushbackReader. r))))
    (do
      (log/warn (str "Configuration file \"" filename "\" not found"))
      {})))
