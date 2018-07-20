(ns kuona-core.http
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http-client]
            [kuona-core.util :as util]))

(def json-headers {"content-type" "application/json; charset=UTF-8"})

(defn parse-json-body
  [response]
  (util/parse-json (:body response)))


(defn build-json-request
  [content]
  {:headers json-headers
   :body    (generate-string content)})

(defn- json-call
  ([f url body]
   (parse-json-body (f url (build-json-request body))))
  ([f url]
   (parse-json-body (f url))))

(defn json-get
  "Makes a Json web service call"
  ([url body]
   (parse-json-body (http-client/post url (build-json-request body))))
  ([url]
   (parse-json-body (http-client/get url {:headers json-headers}))))

(defn json-post
  "Makes a Json web service call"
  [url request]
  (parse-json-body (http-client/post url (build-json-request request))))

(defn json-put
  [url request]
  (parse-json-body (http-client/put url (build-json-request request))))

(defn head [url]
  (http-client/head url))

(defn delete [url]
  (http-client/delete url {:headers json-headers}))

