(ns kuona-api.test-helpers
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(defn mock-json-post
  [app url body]
  (app (-> (mock/request :post url
                         (generate-string body))
           (mock/content-type "application/json"))))

(defn mock-json-put
  [app url body]
  (app (-> (mock/request :put url
                         (generate-string body))
           (mock/content-type "application/json"))))


(defn parse-json-response
  [response]
  (try
    (parse-string (:body response) true)
    (catch Exception _
        {})))
