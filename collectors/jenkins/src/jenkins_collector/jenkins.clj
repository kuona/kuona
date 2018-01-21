(ns jenkins-collector.jenkins
  (:require [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.util :as util]
            [clj-http.client :as http]
            [clojure.string :as string]
            [slingshot.slingshot :refer :all]
            [clojure.xml :as cxml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :refer :all]))

(defn job-to-job
  [job]
  {:name (:name job)
   :url  (:url job)})

(defn read-jenkins-jobs
  [connection url]
  (let [response (connection url)
        result (map job-to-job (:jobs response))]
    result))



(defn read-description
  [x]
  (xml1-> x
          (tag= :project)
          (tag= :description)
          text))

(defn read-scm
  "Read source control elements from supplied XML ZIP document"
  [x]
  {:scm :git
   :url (xml1-> x
                (tag= :project)
                (tag= :scm)
                (tag= :userRemoteConfigs)
                (tag= :hudson.plugins.git.UserRemoteConfig)
                (tag= :url)
                text)
   :ref (xml1-> x
                (tag= :project)
                (tag= :scm)
                (tag= :branches)
                (tag= :hudson.plugins.git.BranchSpec)
                (tag= :name)
                text)})

(defn zip-str [s]
  (zip/xml-zip 
   (cxml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))

(defn parse-xml-response
  [response]
  (let [body (-> response :body)]
    (zip-str body)))

(defn read-jenkins-job-configuration
  [connection job]
  (let [uri      (str (:url job) "config.xml")
        response (connection uri parse-xml-response)]

    (merge (read-description response)
           (read-scm response))))

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
  (fn
    [url content-parser]
    (let [uri (json-url url)
          http-credentials (http-credentials (:username credentials) (:password credentials))
          response (http/get uri http-credentials)
          content (content-parser response)]
      content)
    [url]
    (http-source url util/parse-json-body)))

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
    (try+
      (log/info "put-build!" url (-> build :build :url))
      (http/post url (build-json-request build))
      (catch [:status 404] {:keys [request-time headers body]}
        (log/warn "Failed to put build" url build "response" body))
      (catch [:status 500] {:keys [request-time headers body]}
        (log/warn "Failed to put build" url build "response" body))
      (catch Object _
        (log/warn "Failed to put build" url build)
        (throw+)))))

(defn upload-metrics
  [metrics api]
  (log/info "upload-metrics" (count metrics) "metrics to" api)
  (doall (map (fn [b] (put-build! b api)) metrics)))

(defn collect-metrics-alt
  [connection url]
  (log/info "Collecting metrics from" url)
  (let [build-jobs (read-jenkins-jobs connection url)
        build-log (read-jenkins-builds connection build-jobs)
        build-metrics (map #(read-build-metric connection url %) build-log)]
    build-metrics))


(defn read-build-metrics
  [connection server builds]
  (map (fn [build]
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
       builds))

(defn put-builds! [api builds]
  (doall (map (fn [build] (put-build! build api)) builds)))

(defn collect-metrics [connection url api]
  (put-builds! api (read-build-metrics connection url (read-jenkins-builds connection (read-jenkins-jobs connection url)))))

