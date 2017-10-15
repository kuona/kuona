(ns jenkins-collector.jenkins
  (:require [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.util :as util]
            [clj-http.client :as http]
            [clojure.string :as string]))

(defn job-to-job
  [job]
  {:name (:name job)
   :url  (:url job)})

(defn read-jenkins-jobs
  [connection url]
  (let [response (connection url)
        result (map job-to-job (:jobs response))]
    result))

(defn json-url
  [path]
  (if (string/ends-with? path "/") (str path "api/json") (str path "/api/json")))

(defn http-credentials
  [username password]
  (cond
    (not (or (nil? username) (nil? password))) {:basic-auth [username password]}
    :else {}))

(defn http-source
  [credentials]
  (fn [url]
    (let [uri (json-url url)
          http-credentials (http-credentials (:username credentials) (:password credentials))
          response (http/get uri http-credentials)
          content (util/parse-json-body response)]
      content)))

(defn read-build
  [connection job]
  (log/info "Reading builds for job" job)
  (let [build (connection (:url job))]
    (map (fn [b] {:name   (:name build)
                  :number (:number b)
                  :url    (:url b)
                  :source b}) (:builds build))))

(defn read-jenkins-builds
  [connection jobs]
  (flatten (map #(read-build connection %) jobs)))

(defn read-build-metric
  [connection server build]
  (log/info "read-build-metric" (-> build :url))
  (let [content (connection (-> build :url))]
    {:build
     {:id        (util/uuid-from (:url build))
      :source    server
      :timestamp (:timestamp content)
      :name      (:name build)
      :system    :jenkins
      :url       (:url build)
      :number    (:number content)
      :duration  (:duration content)
      :result    (:result content)
      :collected (util/timestamp)
      :collector {:name    :kuona-jenkins-collector
                  :version (util/get-project-version 'kuona-jenkins-collector)}
      :jenkins   content}}))

(defn build-json-request
  [content]
  {:headers {"content-type" "application/json; charset=UTF-8"}
   :body    (generate-string content)})

(defn put-build!
  [build api]
  (let [url (str api "/api/builds")]
    (log/info "put-build!" url)
    (http/post url (build-json-request build))))

(defn upload-metrics
  [metrics api]
  (log/info "upload-metrics" (count metrics) "metrics to" api)
  (doall (map (fn [b] (put-build! b api)) metrics)))

(defn collect-metrics
  [connection url]
  (log/info "Collecting metrics from" url)
  (let [build-jobs (read-jenkins-jobs connection url)
        build-log (read-jenkins-builds connection build-jobs)
        build-metrics (map #(read-build-metric connection url %) build-log)]
    build-metrics))
