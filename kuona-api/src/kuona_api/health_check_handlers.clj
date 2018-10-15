(ns kuona-api.health-check-handlers
  (:require [ring.util.response :as response]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer :all])
  (:gen-class)
  (:import (java.net MalformedURLException URL)))

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
          (map valid-endpoint? endpoints))  )

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

(defn new-health-check
  [health-check]
  (let [status (valid-health-check? health-check)]
    (if (-> status :valid)
      (response/created (kuona-api.core.store/put-document health-check kuona-api.core.stores/health-check-store kuona-api.core.util/uuid))
      (response/status (response/response status) 401)
      )))
