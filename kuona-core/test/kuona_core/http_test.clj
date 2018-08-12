(ns kuona-core.http-test
  (:require [midje.sweet :refer :all])
  (:require [kuona-core.http :refer [build-json-request]]))

(facts "about json requests"
       (fact "content type is application/json"
             (:headers (build-json-request {})) => {"content-type" "application/json; charset=UTF-8"})
       (fact "handles empty json object"
             (:body (build-json-request {})) => "{}")
       (fact "converts hashmap to json text"
             (:body (build-json-request {:key :value})) => "{\"key\":\"value\"}"))
