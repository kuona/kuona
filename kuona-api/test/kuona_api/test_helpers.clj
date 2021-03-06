(ns kuona-api.test-helpers
  (:require [cheshire.core :refer :all]
            [midje.sweet :refer :all]
            [kuona-api.core.util :as util]
            [ring.mock.request :as mock]))

(defn mock-json-post
  [app url body]
  (app (-> (mock/request :post url
                         (generate-string body))
           (mock/content-type "application/json"))))

(defn mock-json-request
  [app method url body]
  (let [response (app (-> (mock/request (keyword method) url
                                        (generate-string body))
                          (mock/content-type "application/json")))]
    (util/json-decode-body response)))


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
