(ns test-service.handler-test
  (:require [ring.mock.request :as mock]
            [cheshire.core :refer :all]
            [midje.sweet :refer :all]
            [test-service.handler :refer :all]))

(defn parse-json-response
  [response]
  (parse-string (:body response) true))

(facts "about test service"
       (fact
        (let [response (app (mock/request :get "/"))]
          (:status response) => 200))
       (fact "no route returns 404"
        (let [response (app (mock/request :get "/invalid"))]
          (:status response) => 404))
       (fact "status service response structure"
             (let [body (parse-json-response (app (mock/request :get "/status")))]
                   (:status body) => "UP"))
       (fact "down endpoint is down"
             (let [body (parse-json-response (app (mock/request :get "/status/down")))]
               (:status body) => "DOWN"))
       (fact "up endpoint is up"
             (let [body (parse-json-response (app (mock/request :get "/status/up")))]
               (:status body) => "UP")))
