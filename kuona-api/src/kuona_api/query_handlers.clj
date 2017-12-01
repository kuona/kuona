(ns kuona-api.query-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-core.metric.store :as store]
            [kuona-core.util :as util]
            [kuona-api.build-handlers :as build]
            [kuona-api.snapshot-handlers :as snapshots]
            [kuona-api.repository-handlers :as repo]
            [ring.util.response :refer [resource-response response status not-found]])
  (:gen-class))


(def sources
  {:builds       {:id          :builds
                  :index       build/build-mapping
                  :description "Build data - software construction data read from Jenkins"}
   :snapshots    {:id          :snapshots
                  :index       snapshots/snapshots
                  :description "Snapshot data from source code analysis"}
   :repositories {:id          :repositories
                  :index       repo/repositories
                  :description "Captured repository data"}
   :commits      {:id          :commits
                  :index       repo/commits
                  :description "Captured commit data"}})

(defn get-sources
  "Returns a list of available sources that can be queried."
  []
  (log/info "Query sources")
  (response {:sources (map (fn [k] {:id k :name k :description (-> sources k :description)}) (keys sources))}))

(defn make-query
  "Calls the backing store to make query the index"
  [source query]
  (let [result (store/query source query)]
    (cond
      (-> result :error) result)
    :else result))

(defn query-source
  "Query an available source"
  [source-name query]
  (let [id (keyword source-name)]
    (log/info "Query source" source-name)
    (cond
      (get sources id) (response (make-query (-> sources id) query))
      :else            (not-found {:error {:description "Invalid source name in query"}}))))

(defn source-schema
  "Handles requests for source schemas. Returns the requested schema or an error if the schema is not known"
  [source-name]
  (log/info "Query source schema for" source-name)
  (let [id (keyword source-name)]
    (cond
      (get sources id) (response {:schema (store/read-schema (-> sources id))})
      :else            (not-found {:error {:description "Invalid source name in request for schema"}}))))
