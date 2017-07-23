(ns kuona-core.maven-test
  (:require [midje.sweet :refer :all]
            [clojure.data.xml :refer :all]
            [kuona-core.maven :refer :all]
            [kuona-core.util :refer :all]))

(defn tag
  [name value]
  (element name {} value))

(def maven-dependency-plugin
  (element :dependency {} (list
                           (tag :groupId "org.apache.maven.plugins")
                           (tag :artifactId "maven-dependency-plugin")
                           (tag :version "2.10")
                           (tag :type "maven-plugin"))))

(facts "about loading pom.xml files"
       (fact
        (let [pom (load-pom-file "test/pom.xml")]
          (first (:content (first (filter #(= (:tag %) :modelVersion) (:content pom))))) => "4.0.0"
          )))
