(ns kuona-api.core.healthcheck
  (:require [slingshot.slingshot :refer :all]
            [kuona-api.core.http :as http]
            [clj-http.client :as http-client]
            [kuona-api.core.util :as util]
            [kuona-api.core.store :as store]
            [kuona-api.core.stores :as stores]))


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
                (select-keys [:info :health])))
  )


(defn merge-list
  "Merge the maps in the list into a single map"
  [list]
  (if (first list)
    (merge (first list) (merge-list (rest list)))
    {}))

(defn read-link
  "Takes a map containing an :href key. Retrieves the JSON response from the supplied href and returns the value. Returns {} if the key does not exist"
  [link]
  (if (:href link)
    (http/json-get (:href link))
    {}))

(defn http-get-check-health
  [url]
  (try+
    (http-client/get url)
    (catch java.net.UnknownHostException e
      {:status      :failed
       :description (:message &throw-context)})
    (catch java.net.ConnectException e
      {:status      :failed
       :description "Connection refused"})))

(defn spring-actuator-health
  "Given a Spring actuator endpoint returns the health and info endpoints as a single map."
  [url]
  (let [actuator-links (http/json-get url)
        links          (filter-actuator-links actuator-links)]
    (merge-list (map read-link (vals links)))))

(defn health-check
  "Executes a healthcheck and returns the results of the healthcheck operation"
  [hc]
  (let [encoding (-> hc :encoding)]
    (try+
      (cond (= encoding :json)
            (http/json-get (-> hc :href))
            :else {:status      :failed
                   :description (str "Unrecognised healthcheck encoding " encoding)})
      (catch java.net.UnknownHostException e
        {:status      :failed
         :description (:message &throw-context)})
      (catch java.net.ConnectException e
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

