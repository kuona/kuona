(ns kuona-core.gradle
  (:require [clojure.tools.logging :as log])
  (:import (org.gradle.tooling GradleConnector)
           (org.gradle.tooling.model.idea IdeaProject))
  (:gen-class))

(defn gradle-dependency
  [d]
  (merge {:scope    (.getScope (.getScope d))
          :exported (.getExported d)}
         (select-keys (bean (.getGradleModuleVersion d)) [:group :name :version])))

(defn gradle-module
  [m]
  (let [name         (.getName m)
        dependencies (.getDependencies m)]
    {:name         name
     :description  (.getDescription m)
     :dependencies (into [] (map gradle-dependency dependencies))}))


(defn analyse-gradle-project
  [path]
  (log/info "Collecting gradle build information from " path)
  (let [g            (GradleConnector/newConnector)
        connector    (.forProjectDirectory g (clojure.java.io/as-file path))
        connection   (.connect connector)
        modelBuilder (.model connection IdeaProject)
        project      (.get modelBuilder)]
    (let [modules (.getModules project)
          result  {:project {:description (.getDescription project)
                             :jdk         (.getJdkName project)
                             :name        (.getName project)
                             :modules     (into [] (map gradle-module modules))}
                   }
          ]
      (.close connection)
      (log/info "Collected build information from '" path "' is " result)
      result)))
