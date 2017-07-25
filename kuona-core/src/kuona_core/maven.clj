(ns kuona-core.maven
  (:require [clojure.java.shell :as shell]
            [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [clojure.data.xml :refer :all]
            [kuona-core.util :refer :all])
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

(defn analyse-pom-file
  [path]
  (let [content (-> (load-pom-file path) :content)]
    {:artifact {:name          (tag-content content :name)
                :groupId       (tag-content content :groupId)
                :artifactId    (tag-content content :artifactId)
                :version       (tag-content content :version)
                :packaging     (tag-content content :packaging)
                :url           (tag-content content :url)
                :inceptionYear (tag-content content :inceptionYear)
                :description   (tag-content content :description)}}))


