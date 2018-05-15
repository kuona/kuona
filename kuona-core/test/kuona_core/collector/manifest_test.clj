(ns kuona-core.collector.manifest-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [kuona-core.collector.manifest :as manifest]
            [kuona-core.util :as util]
            [clojure.java.io :as io])
  (:import (java.io File)))

(facts "about manifests"
       (let [m (manifest/from-file "test/test-manifest.yaml")]
         (fact m => {:manifest {:components  [{:dependencies [{:id "kuona-api"}]
                                               :description  "Angular Kuona UI"
                                               :id           "dashboard"
                                               :path         "/dashboard"}
                                              {:dependencies [{:id "elasticsearch" :kind "database"}]
                                               :description  "Web service for captured data"
                                               :id           "kuona-api"
                                               :path         "/kuona-api"}]
                                :description "Kuona manifest for kuona-project"
                                :version     0.1}})
         (fact (manifest/components m) => #{{:kind "database" :name "elasticsearch"}
                                            {:kind "component" :name "dashboard"}
                                            {:kind "component" :name "kuona-api"}})))
(facts "about manifest to plantuml conversion"
       (fact "empty manifest"
             (manifest/manifest-uml {}) => "@startuml\n@enduml")
       (fact "single component manifest"
             (manifest/manifest-uml {:manifest {:components [{:kind "component" :id "kuona-api"}]}}) => "@startuml
component kuona-api
@enduml")

       (fact "single component with dependency manifest"
             (manifest/manifest-uml {:manifest {:components [{:kind         "component" :id "kuona-api"
                                                              :dependencies [{:id   "db"
                                                                              :kind "database"
                                                                              }]}]}}) => "@startuml
database db
component kuona-api
kuona-api --> db
@enduml")

       (fact "plantuml dependencies"
             (manifest/plantuml-dependencies [{:from "from" :to "to"}]) => ["from --> to"]))


(facts "about dependency extractions"
       (fact
         (manifest/dependencies {:manifest {:components [{:kind         "component" :id "kuona-api"
                                                          :dependencies [{:id   "db"
                                                                          :kind "database"}
                                                                         {:id "ext"}]}]}}) => [{:from "kuona-api" :to "db"} {:from "kuona-api" :to "ext"}]))

(facts "about manifest file finding"
       (fact
         (manifest/manifest-file "test/no-manifest")) => true)

(defn remove-existing-file
  "Tests for the file existing. Deletes the file and returns true if the file exists or false otherwise"
  [& args]
  (let [^File f (apply io/file args)]
    (.delete f)))

(facts "about manifest collection"

       (fact "generates default diagram if manifest missing"
             (let [repository-id (util/uuid)]
               (manifest/collect repository-id "src" "test") => nil
               (remove-existing-file "test" (str repository-id ".svg"))))

       (fact "generates diagram for manifest missing"
             (let [repository-id (util/uuid)]
               (manifest/collect repository-id ".." "test") => nil

               (remove-existing-file "test" (str repository-id ".svg")))))
