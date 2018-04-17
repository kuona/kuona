(ns kuona-api.metric-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-api.environments :refer :all]
            [kuona-core.store :as store]
            [ring.util.response :refer [resource-response response status not-found]]
            [kuona-core.stores :refer [metrics-store]]
            [compojure.route :as route])
  (:gen-class))

(defn page-link
  [base page-number]
  (str base "?page=" page-number))

(defn get-metrics
  [mapping, search page]
  (route/not-found (str "Get metric by name " mapping " No longer supported"))
  ;(response (store/search (store/mapping mapping kuona-metrics-index) search 100 page #(page-link (str "/api/mapping/" mapping) %)))
  )

(defn get-metrics-count
  [mapping]
  (route/not-found (str "Get metric by name  " mapping " No longer supported"))
  ;(response (store/get-count (store/mapping mapping kuona-metrics-index)))
  )
