(ns kuona-api.metric-handlers
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [kuona-api.environments :refer :all]
            [kuona-core.store :as store]
            [kuona-core.stores :as stores]
            [ring.util.response :refer [resource-response response status not-found]]
            [kuona-core.stores :refer [metrics-store]]
            [compojure.route :as route]
            )
  (:gen-class))

(defn page-link
  [base page-number]
  (str base "?page=" page-number))

(defn get-metrics
  [source-name, search page]

  (cond
    (get stores/sources (keyword source-name)) (response (store/search (:index (get stores/sources (keyword source-name))) search 100 page #(page-link (str "/api/mapping/" source-name) %)))
    :else (route/not-found (str "Search metrics: " source-name " Not found"))))

(defn get-metrics-count
  [source-name]
  (cond
    (get stores/sources (keyword source-name)) (response (store/get-count (:index (get stores/sources (keyword source-name)))))
    :else (route/not-found (str "Get metric by name '" (keyword source-name) "' unknown source name available names are " (string/join " " (keys stores/sources))))))
