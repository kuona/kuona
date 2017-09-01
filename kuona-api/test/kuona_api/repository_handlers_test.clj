(ns kuona-api.handler-test
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http]
            [kuona-api.environments :refer :all]
            [kuona-api.repository-handlers :as :h]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(facts "about commit pagination"
       (fact (h/commits-page-link 1 11) => "/api/repositories/1/commits?page=11"))

(facts "about commit handling"
       (fact "returns error if commit does not have an id"
             (let [response (mock-json-put app "/api/repositories/1/commits" {})]
               (:status response) => 400))
       (fact "returns commit if created"
             (:status (mock-json-put app "/api/repositories/1/commits" {:id 234})) => 200
             (provided (http/put "http://localhost:9200/kuona-repositories/commits/234" {:headers {"content-type" "application/json; charset=UTF-8"},
                                                                                         :body    "{\"id\":234,\"repository_id\":\"1\"}"}) => {:status 200 :body (generate-string {:id 234})}))
       
       )
