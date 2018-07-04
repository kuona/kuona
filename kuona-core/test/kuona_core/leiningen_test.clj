(ns kuona-core.leiningen-test
  (:require [midje.sweet :refer :all])
  (:require [kuona-core.leiningen :refer :all]))


(facts "about project file reading"
       (let [project (read-leiningen-project "project.clj")]
         (fact "project contains a name"
               (-> project :project :name) => "kuona-core")
         (fact "project contains a description"
               (-> project :project :description) => "Kuona core library")
         (fact "project contains a version"
               (-> project :project :version) => "0.0.2")
         (fact "project has dependencies"
               (-> project :project) => (contains {:dependencies anything}))))
