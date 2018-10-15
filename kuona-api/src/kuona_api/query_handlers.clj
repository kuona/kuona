(ns kuona-api.query-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-api.core.store :as store]
            [kuona-api.build-handlers :as build]
            [kuona-api.snapshot-handlers :as snapshots]
            [kuona-api.repository-handlers :as repo]
            [kuona-api.core.stores :refer [sources]]
            [ring.util.response :refer [resource-response response status not-found]])
  (:gen-class))




(defn get-sources
  "Returns a list of available sources that can be queried."
  []
  (log/info "Query sources")
  (response {:sources (map (fn [[k v]] v) sources)}))

(defn make-query
  "Calls the backing store to make query the index"
  [source query]
  (log/info "make-query" (:id source)  query)
  (let [result (store/query source query)]
    (cond
      (-> result :error) result)
    :else result))

(defn query-source
  "Query an available source"
  [source-name query]
  (let [id     (keyword source-name)
        source (-> sources id)]
    (log/info "Query source '" source-name "'")
    (cond
      source (response (make-query source query))
      :else (not-found {:error {:description "Invalid source name in query"}}))))

(defn source-schema
  "Handles requests for source schemas. Returns the requested schema or
  an error if the schema is not known"
  [source-name]
  (log/info "Query source schema for '" source-name "'")
  (let [id     (keyword source-name)
        source (-> sources id)]
    (cond
      source (response {:schema (store/read-schema source)})
      :else (not-found {:error {:description "Invalid source name in request for schema"}}))))
