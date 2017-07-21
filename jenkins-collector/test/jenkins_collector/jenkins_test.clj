(ns jenkins-collector.jenkins-test
  (:require [clj-http.client :as client]
            [midje.sweet :refer :all]
            [jenkins-collector.jenkins :refer :all]
            [cheshire.core :refer :all]
            [clojure.tools.logging :as log]))

(def stubbed-home-response
  {:jobs
                    [{:_class "hudson.model.FreeStyleProject",
                      :name   "boost-program-options",
                      :url    "http://jenkins.com/job/boost-program-options/",
                      :color  "blue"}]})

(def stubbed-build-history

  {:builds [{:_class "hudson.model.FreeStyleBuild",
             :number 6,
             :url    "http://jenkins.com/job/intercept/6/"}
            {:_class "hudson.model.FreeStyleBuild",
             :number 5,
             :url    "http://jenkins.com/job/intercept/5/"}
            {:_class "hudson.model.FreeStyleBuild",
             :number 4,
             :url    "http://jenkins.com/job/intercept/4/"}
            {:_class "hudson.model.FreeStyleBuild",
             :number 0,
             :url    "http://jenkins.com/job/intercept/0/"}],
   :name "intercept"})


(def stubbed-build-result
  {:number            4,
   :duration          33835,
   :result            "SUCCESS",
   :id                "2013-12-20_12-42-32",
   :_class            "hudson.model.FreeStyleBuild",
   :executor          nil,
   :timestamp         1387543352000})

(defn stubbed-connection
  [url]
  (log/info "Stubbed request for " url)
  (case url
    "http://jenkins.com/" stubbed-home-response
    "http://jenkins.com/job/boost-program-options/" stubbed-build-history
    "http://jenkins.com/job/boost-program-options/4/" stubbed-build-result
    "http://jenkins.com/job/intercept/6/" stubbed-build-result
    (println "************************************************ No stubbed data" url)))


(facts "jenkins jobs"
       (fact "reads jobs from source"
             (jobs stubbed-connection "http://jenkins.com/") => [{:name "boost-program-options",
                                                                  :url  "http://jenkins.com/job/boost-program-options/"}])
       (fact "builds returns list of builds"
             (builds stubbed-connection (jobs stubbed-connection "http://jenkins.com/")) => [{:name "intercept", :number 6, :url "http://jenkins.com/job/intercept/6/"}
                                                                                              {:name "intercept", :number 5, :url "http://jenkins.com/job/intercept/5/"}
                                                                                              {:name "intercept", :number 4, :url "http://jenkins.com/job/intercept/4/"}
                                                                                             {:name "intercept", :number 0, :url "http://jenkins.com/job/intercept/0/"}])
       (let [metrics (collect-metrics stubbed-connection "http://jenkins.com/")
             metric (first metrics)]
         (fact "details the collector"
               (:collector metric) => {:name :kuona-jenkins-collector, :version "0.1"})
         (fact "includes name"
               (:name (:metric metric)) => "intercept")
         (fact "includes source"
               (:source (:metric metric)) => {:system :jenkins, :url "http://jenkins.com/job/intercept/6/"})
         (fact "activity"
               (let [activity (:activity (:metric metric))]
                 (:type activity) => :build
                 (:number activity) => 4
                 (:duration activity) => 33835
                 (:result activity) => "SUCCESS" ))))

