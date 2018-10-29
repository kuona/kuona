(ns kuona-api.core.util
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [cheshire.core :refer :all])
  (:import java.util.Properties
           (java.util UUID Date)
           (java.io StringReader PushbackReader))
  (:gen-class))

(defn string-reader
  [s]
  (StringReader. s))

(defn uuid ^UUID [] (str (UUID/randomUUID)))

(defn uuid-from
  ([a b] (uuid-from (str a b)))
  ([s] (str (UUID/nameUUIDFromBytes (.getBytes s)))))

(defn not-nil? [v] (not (nil? v)))

(defn directory?
  "Tests the path for being a directory"
  [^String path]
  (.isDirectory (io/as-file path)))

(defn file-exists?
  [^String file-path]
  (.exists (io/file file-path)))

(defn timestamp
  "Generate a timestamp for the current instant"
  []
  (new Date))

(defn canonical-path
  "Returns canonical path of a given path"
  [^String path]
  (.getCanonicalPath (io/file path)))

(defn absolute-path
  [^String path]
  (.getAbsolutePath (io/file path)))

(defn file-reader
  [^String path]
  (clojure.java.io/reader path))

(defn find-files
  [^String path]
  (file-seq (clojure.java.io/file path)))

(defn parse-json
  [^String text]
  (parse-string text true))


(defn map-kv
  "takes a map and applies the function to each key/value pair returning
  a map of the result."
  [m f]
  (into {} (map (fn [[k v]] (f k v)) m)))

(defn json-encode-body
  "encodes the :body value as a json string"
  [m]
  (map-kv m (fn [k v]
              [k (cond
                   (= k :body) (generate-string v)
                   :else v)])))

(defn json-decode-body
  "Decode a json encodes body response into a map"
  [m]
  (map-kv m (fn [k v]
              [k (cond
                   (= k :body) (parse-string v true)
                   :else v)])))

(defn get-project-version
  "Reads the project version for the supplied dependency
  
  usage (get-project-version 'projectname)"
  [dep]
  (let [path  (str "META-INF/maven/" (or (namespace dep) (name dep))
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
        (clojure.edn/read (PushbackReader. r))))
    (do
      (log/warn (str "Configuration file \"" filename "\" not found"))
      {})))
