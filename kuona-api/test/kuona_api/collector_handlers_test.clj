(ns kuona-api.collector-handlers-test
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http]
            [kuona-core.util :as util]
            [kuona-api.collector-handlers :as h]
            [kuona-api.test-helpers :as helper]
            [kuona-api.handler :refer :all]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(def collector-activity
  {:id        234
   :collector {:name    "test"
               :version "1.0"}
   :activity  :started
   :timestamp (util/timestamp)})

(def expected-activity
  {:headers {"content-type" "application/json; charset=UTF-8"}
   :body    (generate-string collector-activity)})

(def no-results-response
  {:status 200
   :body   (generate-string {:took      0
                             :timed_out false
                             :_shards   {:total      5
                                         :successful 5
                                         :failed     0}
                             :hits      {:total     0
                                         :max_score nil
                                         :hits      [ ]}
                             })})

(def search-headers {:headers {"content-type" "application/json; charset=UTF-8"}})

(facts "about collector activities"
       (fact "returns collector activity if created"
             (:status (helper/mock-json-post app "/api/collectors/activities" collector-activity)) => 200
             (provided (http/put "http://localhost:9200/kuona-collectors/activity/234" expected-activity) => {:status 200 :body (generate-string {:id 234})}))

       (fact "returns success for empty search"
             (:status (app (mock/request :get "/api/collectors/activities"))) => 200
             (provided (http/get "http://localhost:9200/kuona-collectors/activity/_search?size=100&sort=timestamp:desc" search-headers) => no-results-response))
       
       (fact "returns collector activities with count"
             (keys (helper/parse-json-response (app (mock/request :get "/api/collectors/activities")))) => (contains #{:count :items :links})
             (provided (http/get "http://localhost:9200/kuona-collectors/activity/_search?size=100&sort=timestamp:desc" search-headers) => no-results-response)))
