(ns kuona-api.core.maven
  (:require [clojure.java.shell :as shell]
            [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [clojure.data.xml :refer :all]
            [kuona-api.core.util :refer :all])
  (:import [kuona.maven.dot MavenDotReader])
  (:gen-class))

(defn load-pom-file
  [path]
  (let [input-xml (clojure.java.io/reader path)]
    (parse input-xml)))

(defn find-tag
  [m v]
  (keep #(when (= (val %) v) (key %)) m))

(defn tag-content
  [m tag]
  (first (:content (first (filter #(= (:tag %) tag) m)))))

(defn maven-dependency-tree
  [pom-file-path dot-file]
  (log/info "maven-dependency-tree " pom-file-path " to " dot-file)
  (let [result (shell/sh "mvn"
                         "--file" pom-file-path
                         "dependency:tree"
                         (str "-DoutputFile=" dot-file)
                         "-DoutputType=dot")]
    result))


(defn run-maven-dependency-list
  [pom-file-path]
  (log/info "run-maven-dependency-list " pom-file-path)
  (let [dot-file (canonical-path-from-string "./dependency.dot")
        result   (maven-dependency-tree pom-file-path dot-file)]
    (cond
      (= (:exit result) 0) (MavenDotReader/readDependencies (clojure.java.io/input-stream dot-file))
      :else {})))

;    (clojure.pprint/pprint analysis)
;    (shell/sh "mvn" "--file" pom-file-path "clean")
;    (clojure.java.io/delete-file dot-file)
;    analysis))

(defn analyse-pom-file
  [path]
  (log/info "analyse-pom-file " path)
  (let [content (-> (load-pom-file path) :content)]
    {:artifact {:name          (tag-content content :name)
                :groupId       (tag-content content :groupId)
                :artifactId    (tag-content content :artifactId)
                :version       (tag-content content :version)
                :packaging     (tag-content content :packaging)
                :url           (tag-content content :url)
                :inceptionYear (tag-content content :inceptionYear)
                :description   (tag-content content :description)}
     :dependencies (run-maven-dependency-list path)}))


