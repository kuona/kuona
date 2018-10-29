(ns kuona-api.core.healthcheck
  (:require [slingshot.slingshot :refer :all]
            [kuona-api.core.http :as http]
            [clj-http.client :as http-client]
            [kuona-api.core.util :as util]
            [kuona-api.core.store :as store]
            [kuona-api.core.stores :as stores])
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
    (http-client/get url)
    (catch UnknownHostException e
      {:status      :failed
       :description (str "Unknown host" (:message &throw-context))})
    (catch ConnectException e
      {:status      :failed
       :description (str "Connection refused" (:message &throw-context))})))

(defn spring-actuator-health
  "Given a Spring actuator endpoint returns the health and info endpoints as a single map."
  [url]
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

(defn put-health-check-log
  [log]
  (store/put-document stores/health-check-log-store log))

(defn perform-http-health-checks
  [hc collection-date]
  (doall (fn [health] (store/put-document stores/health-check-log-store (health-check-log hc health collection-date)))
         (map http-get-check-health (-> hc :endpoints))))

(defn perform-spring-actuator-health-check [hc collection-date]
  (doall (fn [health] (store/put-document stores/health-check-log-store (health-check-log hc health collection-date)))
         (map spring-actuator-health (-> hc :endpoints))))

(def checks
  {:HTTP_GET        perform-http-health-checks
   :SPRING_ACTUATOR perform-spring-actuator-health-check})

(defn perform-health-check-error
  [hc collection-date]
  {:error "Unknown health check type"})

(defn health-check-fn [key]
  (or (get checks key) perform-health-check-error))

(defn perform-health-checks
  "Takes a health-check entry and performs the desired checks. Takes a healthcheck request and collection date"
  [hc collection-date]
  (let [hc-fn (health-check-fn (-> hc :type))]
    (hc-fn hc collection-date)))

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
