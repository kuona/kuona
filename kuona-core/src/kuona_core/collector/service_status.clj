(ns kuona-core.collector.service-status
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [clj-http.client :as http]
            [cheshire.core :refer :all]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.simple :refer [schedule with-repeat-count repeat-forever with-interval-in-milliseconds]])
  (:import (java.net InetAddress))
  (:gen-class))

(def cli-options
  [["-k" "--api-key KEY" "API Key used to communicate with the API service"]
   ["-m" "--metrics URL" "The URL of the API service to use"]
   ["-h" "--help"]])

(defn usage
  "Generate a string of help text to the user descibing how the
  application should be used"
  [options-summary]
  (->> ["Kuona HTTP data collector."
        ""
        "Usage: http-collector  [options] urls"
        ""
        "Options:"
        options-summary
        ""
        "Each URL is used to collect metrics. The results of each"
        "request is gathered into a summary for status and component"
        "version. The results are then posted to the URL specified in"
        "the metrics parameter."]
       (string/join \newline)))

(defn error-msg
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit
  "Exit the application with an error and message."
  [status msg]
  (log/info "Exiting with status " status)
  (println "Exiting with status " status " : " msg)
  (System/exit status))

(defn connection-refused
  "Generates a connection refused status message"
  [url message]
  (log/info url message)
  {:url    url
   :status "DOWN",
   :type   "error",
   :error  message})

(defn json-filter
  "Reads the content type from the supplied response. If the content
  is Json it is parsed and returned as a clojure data structure. If
  the content is not Json then an empty structure is returned."
  [response]
  (cond
    (.contains (:Content-Type (:headers response)) "json") (parse-string (:body response) true)
    :else {}))

(defn extract-status
  [content]
  (cond
    (:status content) {:status (:status content)}
    :else nil))

(defn response-status
  [response]
  (cond
    (= (:status response) 200) {}
    :else {:status "DOWN"}))

(defn up-status
  [url response]
  (println response)
  (let [json-response (json-filter response)]
    (log/info url "UP")
    {:url    url
     :status (:status json-response)}))

(defn collect-endpoint
  "Query a single URL, analysing the response or generate a response
  if a connection could not be made"
  [url]
  (try
    (up-status url (http/get url))
    (catch java.net.ConnectException e
      (log/info "Connection refused for " url)
      (connection-refused url "Connection Refused"))))

(defn content-test
  [content test]
  (test content))


(defn update-status
  "Update the environment status at the supplied url"
  [options status]
  (let [base-url (:metrics options)
        url      (str base-url "/status")]
    (log/info "Updating status of " url " to " status)
    (http/post url {:form-params  {:status status}
                    :content-type :json})))

(defn update-version
  "Update the environment version at the supplied url"
  [options version]
  (let [base-url (:metrics options)
        url      (str base-url "/version")]
    (log/info "Updating version of " url " to " version)
    (http/post url {:form-params  {:version version}
                    :content-type :json})))


(defn collect [options arguments]
  (let [f (fn [status] (update-status options status))]
    (map f (map collect-endpoint arguments))))


;; Overall flow
;;
;; Query the status endpoint
;; Convert the response into a status payload
;; Upload to the supplied link
;;
;; (upload url (status (query-status endpoint)))
(defn http-collector
  [args]
  (log/info "Kuona HTTP Collector starting")
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        s (qs/initialize)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count options) 2) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors))
      :else (exit 0 (collect options arguments)))))

(defjob NoOpJob
        [ctx]
        (comment "Does nothing")
        (println "NOOP but logging"))

(defn -main
  [& args]
  (println "Starting")
  (log/info "Collector starting")

  (let [s       (-> (qs/initialize) qs/start)
        job     (j/build
                  (j/of-type NoOpJob)
                  (j/with-identity (j/key "kuona.httpcollector.noop.1")))
        trigger (t/build
                  (t/with-identity (t/key "collector.triggers.1"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (repeat-forever)
                                     ;;                                   (with-repeat-count 10)
                                     (with-interval-in-milliseconds 1000))))]
    (qs/schedule s job trigger))

  ;;(http-collector args)
  )
