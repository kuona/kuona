(ns kuona-api.core.leiningen-test
  (:require [midje.sweet :refer :all])
  (:require [kuona-api.core.leiningen :refer :all]))


(facts "about project file reading"
       (let [project (read-leiningen-project "project.clj")]
         (fact "project contains a name"
               (-> project :project :name) => "kuona-api")
         (fact "project contains a description"
               (-> project :project :description) => "Kuona HTTP API service")
         (fact "project contains a version"
               (-> project :project :version) => "0.0.2")
         (fact "project has dependencies"
               (-> project :project) => (contains {:dependencies anything}))))
