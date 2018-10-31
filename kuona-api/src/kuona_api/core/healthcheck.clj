(ns kuona-api.core.healthcheck
  (:require [slingshot.slingshot :refer :all]
            [kuona-api.core.http :as http]
            [clj-http.client :as http-client]
            [kuona-api.core.util :as util]
            [kuona-api.core.store :as store]
            [kuona-api.core.stores :as stores]
            [clojure.tools.logging :as log])
  (:import (java.net ConnectException
                     UnknownHostException
                     MalformedURLException
                     URL)))

(defn health-check-log
  "Creates a log entry for a health check and a check result."
  [hc health collection-date]
  {:id              (util/uuid)
   :health_check_id (-> hc :id)
   :date            collection-date
   :tags            (-> hc :tags)
   :health          health})

(defn health-key-filter [log-entry]
  (select-keys log-entry [:health]))

(defn health-check-snapshot [logs hc date]
  (log/info "snapshot " logs hc date)
  (merge
    (select-keys hc [:id :type :tags])
    {:date date}
    {:results (into [] (map health-key-filter logs))}))


(defn filter-actuator-links
  "Filters the list of actuator links for health and info references"
  [links]
  (merge {} (-> links
                :_links
                (select-keys [:info :health]))))

(defn merge-map-list
  "Merge the maps in the list into a single map"
  [list]
  (if (first list)
    (merge (first list) (merge-map-list (rest list)))
    {}))

(defn read-href-link
  "Takes a map containing an :href key. Retrieves the JSON response from the supplied href and returns the value. Returns {} if the key does not exist"
  [link]
  (if (:href link)
    (http/json-get (:href link))
    {}))

(defn http-get-check-health
  [url]
  (try+
    (log/info "HTTP Health check for " url)
    (let [response (http-client/get url)]
      {:status        :success
       :response_code (:status response)
       :url           url}
      )
    (catch UnknownHostException e
      {:url         url
       :status      :failed
       :description (str "Unknown host" (:message &throw-context))})
    (catch ConnectException e
      {:url         url
       :status      :failed
       :description (str "Connection refused" (:message &throw-context))})))

(defn spring-actuator-health
  "Given a Spring actuator endpoint returns the health and info endpoints as a single map."
  [url]
  (log/info "Spring actuator health check collection of " url)
  (let [actuator-links (http/json-get url)
        links          (filter-actuator-links actuator-links)]
    (merge-map-list (map read-href-link (vals links)))))

(defn health-check
  "Executes a healthcheck and returns the results of the healthcheck operation"
  [hc]
  (let [encoding (-> hc :encoding)]
    (try+
      (cond (= encoding :json)
            (http/json-get (-> hc :href))
            :else {:status      :failed
                   :description (str "Unrecognised healthcheck encoding " encoding)})
      (catch UnknownHostException e
        {:status      :failed
         :description (:message &throw-context)})
      (catch ConnectException e
        {:status      :failed
         :description "Connection refused"})))
  )

(defn put-health-check-log [log]
  (store/put-document log stores/health-check-log-store))

(defn put-health-check-snapshot [snapshot]
  (store/put-document snapshot stores/health-check-snapshot-store (-> snapshot :id)))

(defn perform-http-health-checks
  [hc collection-date]
  (log/info "HTTP health check")
  (let [results  (map http-get-check-health (-> hc :endpoints))
        x        (log/info "results" results)
        logs     (map (fn [health] (health-check-log hc health collection-date)) results)
        y        (log/info "logs" logs)
        snapshot (health-check-snapshot logs hc collection-date)]

    (log/info "snapshot" snapshot)
    (put-health-check-snapshot snapshot)
    (doall (map put-health-check-log logs))))

(defn perform-spring-actuator-health-check
  [hc collection-date]
  (log/info "Spring actuator health check")
  (doall (map (fn [health] (store/put-document (health-check-log hc health collection-date) stores/health-check-log-store)) (map spring-actuator-health (-> hc :endpoints)))))

(def checks
  {:HTTP_GET        perform-http-health-checks
   :SPRING_ACTUATOR perform-spring-actuator-health-check})

(defn perform-health-check-error
  [hc collection-date]
  (log/warn "No available handler for health check" hc (type (keyword (-> hc :type))))
  {:error (str "Unknown health check type " (-> hc :type))})

(defn health-check-fn
  [key]
  (or (get checks key) (get checks (keyword key)) perform-health-check-error))

(defn perform-health-checks
  "Takes a health-check entry and performs the desired checks. Takes a healthcheck request and collection date"
  [hc collection-date]
  (log/info "Running health check " hc)
  (let [hc-fn (health-check-fn (-> hc :type))]
    (let [log-entry (hc-fn hc collection-date)]
      (log/info "Log entry" log-entry))))

(defn valid-endpoint?
  ([] false)
  ([url]
   (try+
     (URL. url) true
     (catch MalformedURLException _ false))))

(defn valid-endpoints?
  [endpoints]
  (reduce (fn
            ([] false)
            ([a b] (and a b)))
          (map valid-endpoint? endpoints)))

(defn valid-health-check? [health-check]
  (let [valid-type      (contains? (hash-set "HTTP_GET" "SPRING_ACTUATOR") (-> health-check :type))
        tags            (or (-> health-check :tags) [])
        has-tags        (> (count tags) 0)
        endpoints       (or (-> health-check :endpoints) [])
        has-endpoints   (> (count endpoints) 0)
        valid-endpoints (valid-endpoints? endpoints)
        valid           (and valid-type has-tags has-endpoints valid-endpoints)]
    (if valid
      {:valid true}
      {:valid       false
       :description "Health checks require a type (HTTP_GET, SPRING_ACTUATOR. A list of one or more tags and a list of endpoints to check"})))

(defn all-health-checks []
  (store/all-documents stores/health-check-store))
