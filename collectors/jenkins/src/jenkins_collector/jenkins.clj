(ns jenkins-collector.jenkins
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.util :as util]))

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
    (let [uri              (str url "/api/json")
          http-credentials {:basic-auth [(:username credentials) (:password credentials)]}
          response         (client/get uri http-credentials)
          content          (parse-json (:body response))]
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
  (new java.util.Date))

(defn read-build-metric
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


(defn collect-metrics
  [connection url]
  (log/info "Collecting metrics from" url)
  (let [build-jobs (jobs connection url)
        build-log  (builds connection build-jobs)]
    (map #(read-build-metric connection url %) build-log)))
