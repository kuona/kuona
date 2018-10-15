(ns kuona-api.core.leiningen
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string])
  (:gen-class))

(defn- parse-config-code [[_ project-name version & params]]
  (merge {:name (str project-name) :version version}
         (apply hash-map params)))

(defn- symbol-to-hash
  [s] {:name (name s) :group (namespace s)})

(defn- parse-exclusions
  [e]
  (cond
    (get e 2) {:exclusions (into [] (map symbol-to-hash (get e 3)))}
    :else {}))

(defn parse-dependency
  [e]
  (let [id (first e)]
    (merge (symbol-to-hash id)
           {:version (second e)}
           (parse-exclusions e))))

(defn read-leiningen-project [path]
  (log/info "Collecting clojure (leiningen) build information from " path)
  (let [project (parse-config-code (read-string (slurp path)))]

    {:project (merge project
                     {:dependencies (into [] (map parse-dependency (:dependencies project)))}
                     {:plugins (into [] (map parse-dependency (:plugins project)))})}

    )
  )
