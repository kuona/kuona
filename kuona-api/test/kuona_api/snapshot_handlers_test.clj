(ns kuona-api.handler-test
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http]
            [kuona-api.environments :refer :all]
            [kuona-api.repository-handlers :as :h]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(facts "about migrated route mapping"
       (fact "about build tools"
             (parse-json-response (app (mock/request :get "/api/build/tools"))) => three-buckets
             (provided (http/post anything anything) => {:body (generate-string three-build-buckets)})))
