(ns kuona-core.http-test
  (:require [midje.sweet :refer :all])
  (:require [slingshot.slingshot :refer [throw+]])
  (:require [kuona-core.http :refer [build-json-request wrap-http-call]]))

(facts "about http exception handling"
       (fact (wrap-http-call (fn [] true)) => true)

       (fact "400" (wrap-http-call (fn [] (throw+ {:status 400 :request-time 0 :headers {} :body "{}"}))) => {:status :error})
       (fact "404" (wrap-http-call (fn [] (throw+ {:status 404 :request-time 0 :headers {} :body "{}"}))) => {:status :error :cause 404})
       (fact "Exceptions are turned into error messages" (wrap-http-call (fn [] (throw (RuntimeException. "Test exception message")))) => (contains {:message "Test exception message" :status :error})))


(facts "about json requests"
       (fact "content type is application/json"
             (:headers (build-json-request {})) => {"content-type" "application/json; charset=UTF-8"})
       (fact "handles empty json object"
             (:body (build-json-request {})) => "{}")
       (fact "converts hash map to json text"
             (:body (build-json-request {:key :value})) => "{\"key\":\"value\"}"))
