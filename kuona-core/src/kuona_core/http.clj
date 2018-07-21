(ns kuona-core.http
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http-client]
            [kuona-core.util :as util])
  (:gen-class))

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
   (parse-json-body (f url {:headers json-headers}))))

(defn json-get
  "Makes a Json web service call"
  ([url body]
   (json-call http-client/post url body))
  ([url]
   (json-call http-client/get url)))

(defn json-post
  "Makes a Json web service call"
  [url request]
  (json-call http-client/post url request))

(defn json-put
  [url request]
  (json-call http-client/put url request))

(defn head [url]
  (http-client/head url))

(defn delete [url]
  (json-call http-client/delete url))

