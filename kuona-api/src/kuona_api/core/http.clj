(ns kuona-api.core.http
  (:require [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [clj-http.client :as http-client]
            [slingshot.slingshot :refer :all]
            [kuona-api.core.util :as util])
  (:gen-class))

(def json-headers {"content-type" "application/json; charset=UTF-8"})

(defn wrap-http-call
  [f]
  (try+
    (f)
    (catch [:status 400] {}
           (log/info "Bad request")
      {:status :error
       :cause  400})
    (catch [:status 404] {}
           (log/info "Not authorized")
      {:status :error
       :cause  404})
    (catch [:status 503] {}
           (log/info "Service not available")
      {:status :error
       :cause  503})
    (catch Object _
      (log/error "Unexpected exception " (:message &throw-context))
      {:status  :error
       :message (:message &throw-context)
       :cause   (:cause &throw-context)})))

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
   (log/info "json-get" url body)
   (json-call http-client/get url body))
  ([url]
   (log/info "json-get" url)
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

