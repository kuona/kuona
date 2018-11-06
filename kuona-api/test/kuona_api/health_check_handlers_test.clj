(ns kuona-api.health-check-handlers-test
  (:require [midje.sweet :refer :all]
            [kuona-api.health-check-handlers :refer :all]))

(facts "about defining health-checks"
       (fact "returns 401 for invalid request"
             (new-health-check {}) => (contains {:status 401}))
       (fact "health checks can be tagged"
             (-> (new-health-check {}) :body) => (contains {:valid false})))
