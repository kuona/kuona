(ns kuona-api.snapshot-handlers-test
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http]
            [kuona-api.environments :refer :all]
            [kuona-api.test-helpers :as helper]
            [kuona-api.handler :refer [app]]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(def three-buckets { :buckets [{:key "gradle" :doc_count 748 }
                               {:key "maven" :doc_count 254}
                               {:key "ant" :doc_count 92}]})

(def three-build-buckets
  { :aggregations { :builder three-buckets}})


(facts "about migrated route mapping"
       (fact "about build tools"
             (helper/parse-json-response (app (mock/request :get "/api/build/tools"))) => three-buckets
             (provided (http/post anything anything) => {:body (generate-string three-build-buckets)})))
