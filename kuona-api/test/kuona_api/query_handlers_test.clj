(ns kuona-api.query-handlers-test
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http]
            [clojure.test :refer :all]
            [kuona-api.handler :refer :all]
            [kuona-api.test-helpers :as helper]
            [kuona-core.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(def match-all
  {:query {:match_all {}}})

(facts "about queries"
       (let [std-headers            {"content-type" "application/json; charset=UTF-8"}
             empty-mapping-response (util/json-encode-body {:status 200
                                                            :body   {:index-name {:mappings {:mapping-name {:properties {}}}}}})]
         (fact "query endpoint lists available data sources"
               (:status (app (mock/request :get "/api/query/sources"))) => 200)
         (fact "returns available sources"
               (count (-> (helper/parse-json-response (app (mock/request :get "/api/query/sources"))) :sources)) => 4)
         (fact "returns 404 for invalid index"
               (:status (helper/mock-json-post app "/api/query/invalid" {})) => 404)
         (fact "returns 200 for valid index"
               (:status (helper/mock-json-post app "/api/query/builds" {})) => 200
               (provided (http/get "http://localhost:9200/kuona-builds/builds/_search" {:headers std-headers :body "{}"}) => {:status 200 :body "{}"}
                         (http/get "http://localhost:9200/kuona-builds/_mapping" {:headers std-headers}) => empty-mapping-response))
         (fact "returns content"
               (let [expected-response          {:body    {:count   0
                                                           :results []
                                                           :schema  {:snapshots {}}}
                                                 :headers {"Content-Type" "application/json; charset=utf-8"}
                                                 :status  200}
                     es-stubbed-search-response (util/json-encode-body {:headers std-headers :body match-all})]
                 (helper/mock-json-request app :post "/api/query/snapshots" match-all) => expected-response
                 (provided (http/get "http://localhost:9200/kuona-snapshots/snapshots/_search" es-stubbed-search-response) => (util/json-encode-body {:status 200,
                                                                                                                                                      :body   {:hits {:total 0 :hits []}}}),
                           (http/get "http://localhost:9200/kuona-snapshots/_mapping" {:headers std-headers}) => empty-mapping-response)))))
       
