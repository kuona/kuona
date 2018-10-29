(ns kuona-api.core.maven-test
  (:require [midje.sweet :refer :all]
            [clojure.data.xml :as xml]
            [kuona-api.core.maven :refer :all]
            [kuona-api.core.util :refer :all]
            [kuona-api.core.util :as util]))

(defn tag
  [name value]
  (xml/element name {} value))

(def maven-dependency-plugin
  (xml/element :dependency {} (list
                                (tag :groupId "org.apache.maven.plugins")
                                (tag :artifactId "maven-dependency-plugin")
                                (tag :version "2.10")
                                (tag :type "maven-plugin"))))


(def graph-hopper "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.graphhopper</groupId>
    <artifactId>graphhopper-parent</artifactId>
    <name>GraphHopper Parent Project</name>
    <version>0.10-SNAPSHOT</version>
    <packaging>pom</packaging>
    <url>https://www.graphhopper.com</url>
    <inceptionYear>2012</inceptionYear>
    <description>Super pom of GraphHopper, the fast and flexible routing engine</description>
</project>")


(facts "about loading pom.xml files"
       (fact
         (let [pom (load-pom-file "test/pom.xml")]
           (first (:content (first (filter #(= (:tag %) :modelVersion) (:content pom))))) => "4.0.0"))


       (fact "reads artifact data"
             (let [data (xml/parse (util/string-reader graph-hopper))]
               (analyse-pom-file "some/path") => {:artifact     {:artifactId    "graphhopper-parent"
                                                                 :description   "Super pom of GraphHopper, the fast and flexible routing engine"
                                                                 :groupId       "com.graphhopper"
                                                                 :inceptionYear "2012"
                                                                 :name          "GraphHopper Parent Project"
                                                                 :packaging     "pom"
                                                                 :url           "https://www.graphhopper.com"
                                                                 :version       "0.10-SNAPSHOT"}
                                                  :dependencies {}}
               (provided (load-pom-file "some/path") => data))))
             
