(ns kuona-api.query-handlers-test
  (:require [clojure.test :refer :all]
            [kuona-api.test-helpers :as helper]
            [cheshire.core :refer :all]
            [clj-http.client :as http]
            [kuona-core.util :as util]
            [kuona-api.handler :refer :all]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(def match-all
  {:query {:match_all {}}})

(facts "about queries"
       (fact "query endpoint lists available data sources"
             (:status (app (mock/request :get "/api/query"))) => 200)
       (fact "returns available sources"
             (count (-> (helper/parse-json-response (app (mock/request :get "/api/query"))) :sources)) => 4)
       (fact "returns 404 for invalid index"
             (:status (helper/mock-json-post app "/api/query/invalid" {})) => 404)
       (fact "returns 200 for valid index"
             (:status (helper/mock-json-post app "/api/query/builds" {})) => 200
             (provided (http/get "http://localhost:9200/kuona-builds/builds/_search" {:headers {"content-type" "application/json; charset=UTF-8"}, :body "{}"}) => {:status 200 :body "{}"}))
       (fact "returns content"
             (helper/mock-json-post app "/api/query/snapshots" match-all) => {:body "{\"count\":0,\"results\":[]}", :headers {"Content-Type" "application/json; charset=utf-8"}, :status 200}

             (provided (http/get "http://localhost:9200/kuona-snapshots/snapshots/_search" {:headers {"content-type" "application/json; charset=UTF-8"}, :body (generate-string match-all)}) => {:status 200,
                                                                                                                                                                                                 :body   (generate-string {:hits {:total 0 :hits []}})})))

