(ns kuona-core.collector.service-status-test
  (:require [midje.sweet :refer :all]
            [clj-http.client :as http]
            [cheshire.core :refer :all]
            [kuona-core.collector.service-status :refer :all]))

(facts "Content filtering"
       (fact "Non json content type is empty string"
             (json-filter {:headers {:Content-Type "foobar"}}) => {})
       (fact "Json content type yields body"
             (json-filter {:headers {:Content-Type "json"} :body "{\"foo\":{}}"}) => {:foo {}})
       (fact "application/json content type yields body"
             (json-filter {:headers {:Content-Type "application/json"} :body "{\"foo\":{}}"}) => {:foo {}}))

(facts "Collecting from end points"
       (fact "Service is down if no connection"
             (collect-endpoint "http://localhost:12000") => {:error  "Connection Refused",
                                                             :status "DOWN",
                                                             :type   "error",
                                                             :url    "http://localhost:12000"}
             (provided (http/get anything) =throws=> (java.net.ConnectException.)))

       (fact "Service running and healthy"
             (collect-endpoint "http://kuona.io:9002/status") => {:status "UP",
                                                                  :url    "http://kuona.io:9002/status"}
             (provided (http/get anything) => {:headers {:Content-Type "json"}
                                               :body    (generate-string {:status "UP"})})))

(facts "Content testing"
       (fact "test run against content"
             (content-test {:status "UP"} #(= (:status %1) "UP")) => true)
       (fact "missing key in content fails test"
             (content-test {} #(= (:status %1) "UP")) => false)
       (fact "evaluates string tests"
             (content-test {:status "UP"} (load-string "#(= (:status %1) \"UP\")")) => true))


(facts "integration tests"
       (fact "updates status"
             (update-status {:metrics "http://kuona.io:9001/api/environments/DEVX200"} "UP") => true
             (provided (http/post "http://kuona.io:9001/api/environments/DEVX200/status" {:form-params {:status "UP"} :content-type :json}) => true))
       (fact "updates version number"
             (update-version {:metrics "http://kuona.io:9001/api/environments/DEVX200"} "1.2") => true
             (provided (http/post "http://kuona.io:9001/api/environments/DEVX200/version" {:form-params {:version "1.2"} :content-type :json}) => true)))
