(ns kuona-core.collector.manifest-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [kuona-core.collector.manifest :as manifest]))


(facts "about manifest file finding"
       (fact
         (manifest/manifest-file "test/no-manifest")) => true)

(facts "About cleaned manifest"
       (let [original {:manifest {
                                  :version     "0.1"
                                  :description "Kuona manifest for kuona-project"
                                  :components  [{:component    nil
                                                 :id           "dashboard"
                                                 :description  "Angular Kuona UI"
                                                 :path         "/dashboard"
                                                 :dependencies [{:id "kuona-api"
                                                                 }]
                                                 }
                                                {
                                                 :component    nil
                                                 :id           "kuona-api"
                                                 :path         "/kuona-api"
                                                 :description  "Web service for captured data"
                                                 :dependencies [{
                                                                 :id   "elasticsearch"
                                                                 :kind "database"

                                                                 }]

                                                 }]

                                  }}]
         (fact (manifest/clean-manifest original) => {:manifest {
                                                                 :description "Kuona manifest for kuona-project"
                                                                 :components  [{:id           "dashboard"
                                                                                :description  "Angular Kuona UI"
                                                                                :path         "/dashboard"
                                                                                :dependencies [{:id   "kuona-api"
                                                                                                :kind "component"}]
                                                                                }
                                                                               {:id           "kuona-api"
                                                                                :path         "/kuona-api"
                                                                                :description  "Web service for captured data"
                                                                                :dependencies [{:id   "elasticsearch"
                                                                                                :kind "database"}]
                                                                                }]}})))
