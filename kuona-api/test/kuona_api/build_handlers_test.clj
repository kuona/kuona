(ns kuona-api.build-handlers-test
  (:require [clojure.test :refer :all]
            [kuona-api.test-helpers :as helper]
            [kuona-api.build-handlers :refer :all]
            [cheshire.core :refer :all]
            [clj-http.client :as http]
            [kuona-core.util :as util]
            [kuona-api.collector-handlers :as h]
            [kuona-api.handler :refer :all]
            [midje.sweet :refer :all]))

(def test-build
  {:build
   {:id        "abc123"
    :source    "http://build.server"
    :timestamp 111
    :name      "test build"
    :system    :jenkins
    :url       "some url"
    :number    12345
    :duration  123
    :result    "success"
    :collected (util/timestamp)
    :collector {:name    :kuona-jenkins-collector
                :version (util/get-project-version 'kuona-jenkins-collector)}
    :jenkins   {}}})

(def test-build-es
  {:headers {"content-type" "application/json; charset=UTF-8"}
   :body    (generate-string (-> test-build :build))})

(facts "about build endpoint"
       (fact "returns collector activity if created"
             (:status (helper/mock-json-post app "/api/builds" test-build)) => 200
             (provided (http/put "http://localhost:9200/kuona-builds/builds/abc123" test-build-es) => {:status 200 :body (generate-string {:id "abc123"})})))

