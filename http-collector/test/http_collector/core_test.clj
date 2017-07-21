(ns http-collector.core-test
  (:require [midje.sweet :refer :all]
            [http-collector.core :refer :all]))

(facts "Content filtering"
       (fact "Non json content type is empty string"
             (json-filter {:headers {:Content-Type "foobar"}}) => {})
       (fact "Json content type yields body"
             (json-filter {:headers {:Content-Type "json"}:body "{\"foo\":{}}"}) => {:foo{}})
       (fact "application/json content type yields body"
             (json-filter {:headers {:Content-Type "application/json"}:body "{\"foo\":{}}"}) => {:foo{}}))

(facts "Collecting from end points"
       (fact "Service is down if no connection"
             (collect-endpoint "http://localhost:12000") => {:error "Connection Refused",
                                                             :status "DOWN",
                                                             :type "error",
                                                             :url "http://localhost:12000"})
       (fact "Service running and healthy"
             (collect-endpoint "http://kuona.io:9002/status") => {:status "UP",
                                                                  :url "http://kuona.io:9002/status"}))

(facts "Content testing"
       (fact "test run against content"
             (content-test {:status "UP"} #(= (:status %1) "UP")) => true)
       (fact "missing key in content fails test"
             (content-test {} #(= (:status %1) "UP")) => false)
       (fact "evaluates string tests"
             (content-test {:status "UP"} (load-string "#(= (:status %1) \"UP\")")) => true))

(facts "integration"
       (fact "updates status"
             (update-status {:metrics "http://kuona.io:9001/api/environments/DEVX200"} "UP"))
       (fact "updates version number"
             (update-version {:metrics "http://kuona.io:9001/api/environments/DEVX200"} "1.2")))
