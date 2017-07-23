(ns kuona-core.builder
  (:require [kuona-core.maven :as maven])
  (:gen-class))


(defn build-tool
  [file-path]
  (cond
    (nil? file-path) (fn [p] {})
    (re-matches #"(makefile|Makefile)|(.*/(makefile|Makefile))" file-path)  (fn [p] {:builder "Make" :path p})
    (re-matches #"Rakefile|.*/Rakefile" file-path)  (fn [p] {:builder "Rake" :path p})
    (re-matches #"project.clj|.*/project.clj" file-path)  (fn [p] {:builder "leiningen" :path p})
    (re-matches #"pom.xml|.*/pom.xml" file-path)  (fn [p] (merge (maven/analyse-pom-file p) {:builder "Maven" :path p}))
    :else {}))
