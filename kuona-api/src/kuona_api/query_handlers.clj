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
  {:builds       {:index       build/build-mapping
                  :description "Build data - software construction data read from Jenkins"}
   :snapshots    {:index       snapshots/snapshots
                  :description "Snapshot data from source code analysis"}
   :repositories {:index       repo/repositories
                  :description "Captured repository data"}
   :commits      {:index       repo/commits
                  :description "Captured commit data"}})

(defn get-sources
  "Returns a list of available sources that can be queried."
  []
  (log/info "Query sources")
  (response {:sources (map (fn [k] {:id k :name k :description (-> sources k :description)}) (keys sources))}))

(defn make-query
  "Calls the backing store to make query the index"
  [source query]
  (let [index  (-> sources (keyword source) :index)
        result (store/query index query)]
    (cond
      (-> result :error) {:result result})
    :else {(keyword source)
           {:schema result}}))

(defn query-source
  "Query an available source"
  [source query]
  (let [src (keyword source)]
    (log/info "Query source" src)
    (cond
      (get sources src) (response (make-query (-> sources src :index) query))
      :else             (not-found {:error {:description "Invalid source name in query"}}))))

(defn source-schema
  "Handles requests for source schemas. Returns the requested schema or an error if the schema is not known"
  [source]
  (log/info "Query source schema for" source)
  (let [src (keyword source)]
    (cond
      (get sources src) (response {:schema (store/read-schema source (-> sources src :index))})
      :else             (not-found {:error {:description "Invalid source name in request for schema"}}))))
