(ns jenkins-collector.jenkins
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.tools.logging :as log]))

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
    (let [uri (str url "api/json")
          http-credentials {:basic-auth [(:username credentials) (:password credentials)]}
          response (client/get uri http-credentials)
          content (parse-json (:body response))]
      (log/info "Reading" uri "using" credentials  "Received:" content)
      content)))

(defn read-build
  [connection job]
  (log/info "Reading builds for job" job)
  (let [build (connection (:url job))]
    (log/info "Reading build returned" (:builds build))
    (map (fn [b] {:name   (:name build)
                  :number (:number b)
                  :url    (:url b)}) (:builds build))))

(defn builds
  [connection jobs]
  (flatten (map #(read-build connection %) jobs)))

(defn timestamp
  []
  (new java.util.Date))

(defn read-build-metric
  [connection build]
  (log/info "Read build metric called with " build)
  (let [content (connection (:url build))]
    {:timestamp (:timestamp content)
     :metric    {:type      :build
                 :name      (:name build)
                 :source    {:system :jenkins
                             :url    (:url build)}
                 :activity  {:type     :build
                             :number   (:number content)
                             :id       (:id content)
                             :duration (:duration content)
                             :result   (:result content)}
                 :collected (timestamp)
                 }

     :collector {:name    :kuona-jenkins-collector
                 :version "0.1"}}))


(defn collect-metrics
  [connection url]
  (log/info "Collecting metrics from" url)
  (map #(read-build-metric connection %) (builds connection (jobs connection url))))
