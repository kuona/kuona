(ns kuona-api.core.healthcheck
  (:require [slingshot.slingshot :refer :all]
            [kuona-api.core.http :as http]))

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
