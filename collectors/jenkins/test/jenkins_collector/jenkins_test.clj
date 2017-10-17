(ns jenkins-collector.jenkins-test
  (:require [clj-http.client :as http]
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
  {:number    4,
   :duration  33835,
   :result    "SUCCESS",
   :id        "2013-12-20_12-42-32",
   :_class    "hudson.model.FreeStyleBuild",
   :executor  nil,
   :timestamp 1387543352000})

(defn stubbed-connection
  [url]
  (log/info "Stubbed request for " url)
  (case url
    "http://jenkins.com/" stubbed-home-response
    "http://jenkins.com/job/boost-program-options/" stubbed-build-history
    "http://jenkins.com/job/boost-program-options/4/" stubbed-build-result
    "http://jenkins.com/job/intercept/6/" stubbed-build-result
    (println "************************************************ No stubbed data" url)))

(facts "about reading from Jenkins"
       (let [metrics (collect-metrics stubbed-connection "http://jenkins.com/")
             metric  (first metrics)]
         (fact "details the collector"        (-> metric :build :collector) => {:name :kuona-jenkins-collector, :version "0.0.1"})
         (fact "includes name"                (-> metric :build :name) => "intercept")
         (fact "includes build system type"   (-> metric :build :system) => :jenkins)
         (fact "metric include URL"           (-> metric :build :url) => "http://jenkins.com/job/intercept/6/")
         (fact "extracts the build number"    (-> metric :build :number) => 4)
         (fact "extractst the build duration" (-> metric :build :duration) => 33835)
         (fact "extracts the build result"    (-> metric :build :result) => "SUCCESS")))

(facts "about put-build!"
       (fact "posts build to api"
             (put-build! {} "http://server.com") => "worked"
             (provided
              (http/post "http://server.com/api/builds" {:headers {"content-type" "application/json; charset=UTF-8"}, :body "{}"}) => "worked")))

(facts "about upload-metrics"
       (fact "calls put-build for each metric"
             (upload-metrics '(1 2 3) "http://foo") => '(:result-1 :result-2 :result-3)
             (provided
              (put-build! 1 "http://foo") => :result-1
              (put-build! 2 "http://foo") => :result-2
              (put-build! 3 "http://foo") => :result-3)))

(facts "about jenkins auth"
       (fact "no auth if password missing"
             (http-credentials :username nil) => {})
       (fact "no auth if username missing"
             (http-credentials nil :password) => {})
       (fact "basic auth for username and password"
             (http-credentials "foo" "bar") => {:basic-auth ["foo" "bar"]}))
