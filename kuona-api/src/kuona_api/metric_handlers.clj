(ns kuona-api.metric-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-api.environments :refer :all]
            [kuona-core.metric.store :as store]
            [ring.util.response :refer [resource-response response status]])
  (:gen-class))

(def kuona-metrics-index (store/index :kuona-metrics "http://localhost:9200"))

(defn page-link
  [base page-number]
  (str base "?page=" page-number))

(defn get-metrics
  [mapping, search page]
  (response (store/search (store/mapping mapping kuona-metrics-index) search 100 page #(page-link (str "/api/mapping/" mapping) %))))

(defn get-metrics-count
  [mapping]
  (response (store/get-count (store/mapping mapping kuona-metrics-index))))
