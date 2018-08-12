(ns jenkins-collector.jenkins
  (:require [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.util :as util]
            [kuona-core.http :as json]
            [clj-http.client :as http]
            [clojure.string :as string]
            [slingshot.slingshot :refer :all]
            [clojure.xml :as cxml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :refer :all]))


(defn read-description
  "Reads the project description text from the XML zip representation of the Jenkins Job configuration provided"
  [x]
  {:description (xml1-> x
                        (tag= :project)
                        (tag= :description)
                        text)})

(defn read-scm
  "Read source control elements from supplied XML ZIP of the Jenkins Job configuration document"
  [x]
  {:scm :git
   :url (or
          (xml1-> x
                  (tag= :matrix-project)
                  (tag= :scm)
                  (tag= :userRemoteConfigs)
                  (tag= :hudson.plugins.git.UserRemoteConfig)
                  (tag= :url)
                  text)
          (xml1-> x
                  (tag= :project)
                  (tag= :scm)
                  (tag= :userRemoteConfigs)
                  (tag= :hudson.plugins.git.UserRemoteConfig)
                  (tag= :url)
                  text)
          )



   :ref (or (xml1-> x
                    (tag= :matrix-project)
                    (tag= :scm)
                    (tag= :branches)
                    (tag= :hudson.plugins.git.BranchSpec)
                    (tag= :name)
                    text)
            (xml1-> x
                    (tag= :project)
                    (tag= :scm)
                    (tag= :branches)
                    (tag= :hudson.plugins.git.BranchSpec)
                    (tag= :name)
                    text))
   })

(defn zip-str
  "Returns a XML zip represented by the supplied string."
  [s]
  (zip/xml-zip (cxml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))

(defn parse-xml-response
  "Takes an HTTP response map and extracts the XML body content as an XML Zip object"
  [response]
  (log/trace "parse-xml-response" response)
  (let [body (-> response :body)]
    (zip-str body)))

(defn read-jenkins-job-configuration
  [connection job]
  (try+
    (let [uri (str (:url job) "config.xml")
          response (connection uri parse-xml-response)]

      (log/info "SCM from" uri "is" (read-scm response))
      (merge (read-description response)
             (read-scm response)))
    (catch Object e
      (log/warn "Failed to read jenkins job configuration" e)
      {})))

(defn api-url
  "Adds the JSON api suffix to the supplied path string"
  [path]
  (if (string/ends-with? path "/")
    (str path "api/json")

    (if (string/ends-with? path "xml")
      path
      (str path "/api/json"))))

(defn http-credentials
  "Creates a basic-auth usename and passowrd pair based on the input."
  [username password]
  (cond
    (not (or (nil? username) (nil? password))) {:basic-auth [username password]}
    :else {}))

(defn get-jenkins-content
  [url credentials options]
  (try+
    (let [content-parser (or (first options) json/parse-json-body)
          uri (api-url url)
          http-credentials (http-credentials (:username credentials) (:password credentials))]
      (content-parser (http/get uri http-credentials)))
    (catch [:status 404] {:keys [request-time headers body]}
      (log/warn "404 Failed to get" url))
    (catch [:status 500] {:keys [request-time headers body]}
      (log/warn "500 Failed to get" url))
    (catch Exception e
      (log/warn "Failed to get" url e)
      (throw+))))

(defn http-source
  [credentials]
  (fn
    [url & args] (get-jenkins-content url credentials args)))

(defn job-field-filter
  [job]
  (merge {:name nil :url nil} (select-keys job [:name :url])))

(defn read-jenkins-jobs
  [connection url]
  (map job-field-filter (:jobs (connection url))))

(defn read-builds
  "Read the builds for a particular job"
  [connection job]
  (let [builds (connection (:url job))]
    (map (fn [b] {:name   (:name builds)
                  :number (:number b)
                  :url    (:url b)
                  :source b}) (:builds builds))))

(defn read-all-job-builds
  [connection jobs]
  (flatten
    (map (fn [job]
           (let [config (read-jenkins-job-configuration connection job)
                 builds (read-builds connection job)]
             {:config config
              :builds builds}))
         jobs))

  ;(flatten (map #(log/info (read-jenkins-job-configuration connection %)) jobs))
  ;(flatten (map #(read-builds connection %) jobs))
  )

(defn read-build-metric
  [connection server build]
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
  "Builds a request with JSON content type and hte supplied content as a JSON formatted string"
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
        build-log (read-all-job-builds connection build-jobs)
        build-metrics (map #(read-build-metric connection url %) build-log)]
    build-metrics))


(defn read-build-metrics
  [connection server entries]
  (map (fn [entry]
         {:id (util/uuid)
          :config (-> entry :config)
          :builds (map (fn [build]
                         (let [content (connection (-> build :url))]
                           {:config (-> entry :config)
                            :build
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
                                     :jenkins   content}})) (-> entry :builds))
          }) entries))

(defn put-builds! [api builds]
  (doall (map
           (fn [build] (put-build! build api))
           builds)))

(defn collect-metrics [connection url api]
  (let [jobs (read-jenkins-jobs connection url)]
    (put-builds! api (read-build-metrics connection url (read-all-job-builds connection jobs)))
    )
  )

