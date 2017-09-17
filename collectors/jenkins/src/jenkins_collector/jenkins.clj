(ns jenkins-collector.jenkins
  (:require [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.util :as util]
            [clj-http.client :as http])
  (:import (java.util Date)))

(defn job-to-job
  [job]
  {:name (:name job)
   :url  (:url job)})

(defn jobs
  [connection url]
  (let [response (connection url)
        result (map job-to-job (:jobs response))]
    result))


(defn parse-json
  [content]
  (parse-string content true))

(defn http-source
  [credentials]
  (fn [url]
    (let [uri (str url "/api/json")
          http-credentials {:basic-auth [(:username credentials) (:password credentials)]}
          response (http/get uri http-credentials)
          content (parse-json (:body response))]
      content)))

(defn read-build
  [connection job]
  (log/info "Reading builds for job" job)
  (let [build (connection (:url job))]
    (map (fn [b] {:name   (:name build)
                  :number (:number b)
                  :url    (:url b)
                  :source b}) (:builds build))))

(defn builds
  [connection jobs]
  (flatten (map #(read-build connection %) jobs)))

(defn timestamp
  []
  (new Date))

(defn make-build-metric
  [connection server build]
  (log/info "read-build-metric" (-> build :url))
  (let [content (connection (:url build))]
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
      :collected (timestamp)
      :collector {:name    :kuona-jenkins-collector
                  :version (util/get-project-version 'kuona-jenkins-collector)}
      :jenkins   content}}))

(defn build-json-request
  [content]
  {:headers {"content-type" "application/json; charset=UTF-8"}
   :body    (generate-string content)})

(defn put-build!
  [build url]
  (log/info "put-build!" url)
  (util/parse-json-body (http/put url (build-json-request build))))

(defn upload-metrics
  [build-metrics api]
  (doall #(put-build! % (str api "/api/builds")) build-metrics))

(defn collect-metrics
  [connection url]
  (log/info "Collecting metrics from" url)
  (let [build-jobs (jobs connection url)
        build-log (builds connection build-jobs)
        build-metrics (map #(make-build-metric connection url %) build-log)]
    build-metrics))
