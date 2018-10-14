(ns kuona-api.health-check-handlers
  (:require [ring.util.response :as response])
  (:gen-class))

(defn valid-health-check? [health-check]
  (let [valid-type (contains? [:HTTP_GET :SPRING_ACTUATOR] (-> health-check :type))]
    valid-type))

(defn new-health-check
  [check]

  {:error "HTTP health checks require a valid url"})
