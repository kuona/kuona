(ns kuona-api.core.builder
  (:require [kuona-api.core.maven :as maven]
            [kuona-api.core.gradle :as gradle]
            [kuona-api.core.leiningen :as lein]
            [kuona-api.core.util :refer :all]
            [kuona-api.core.util :as util])
  (:gen-class)
  (:import (java.io File)))

(defn build-tool
  "matches the file path against a defined set of build tools,
  returning a function to analyse that file."
  [file-path project-relative-path]
  (cond
    (nil? file-path) (fn [p] nil)
    (re-matches #"(makefile|Makefile)|(.*/(makefile|Makefile))" file-path) (fn [p] {:builder "Make" :path project-relative-path})
    (re-matches #"Rakefile|.*/Rakefile" file-path) (fn [p] {:builder "Rake" :path project-relative-path})
    (re-matches #"project.clj|.*/project.clj" file-path) (fn [p]
                                                           (merge {:builder "Leiningen" :path project-relative-path}
                                                                  (lein/read-leiningen-project p)))
    (re-matches #"build.gradle|.*/build.gradle" file-path) (fn [p]
                                                             (merge {:builder "Gradle" :path project-relative-path}
                                                                    (gradle/analyse-gradle-project (.getParent (clojure.java.io/as-file p)))))
    (re-matches #"build.xml|.*/build.xml" file-path) (fn [p] {:builder "Ant" :path project-relative-path})
    (re-matches #"pom.xml|.*/pom.xml" file-path) (fn [p]
                                                   (merge {:builder "Maven" :path project-relative-path}
                                                          (maven/analyse-pom-file p)))
    :else (fn [p] nil)))


(defn process-project-file
  "Takes the full path and a project relative path. Runs the
  appropriate build tool using the path but reports using the relative
  path"
  [^File file project-relative-path]
  (let [path   (util/get-absolute-path file)
        result ((build-tool path project-relative-path) path)]
    result))

(defn collect-builder-metrics
  "Scans the supplied path for interesting build files returning the
  analysis result for each file found"
  [path]
  (let [project-path (canonical-path-from-string path)]
    (into [] (filter not-nil? (map #(process-project-file % (subs (util/get-absolute-path %) (count project-path))) (find-files project-path))))))
