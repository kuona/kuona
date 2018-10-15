(ns kuona-api.snapshot-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-api.environments :refer :all]
            [kuona-api.core.store :as store]
            [ring.util.response :refer [resource-response response status]]
            [kuona-api.core.stores :as stores])
  (:gen-class))


(defn build-tool-buckets
  []
  (log/info "build-tool-buckets")
  (let [result  (store/internal-search stores/snapshots-store {:size         0
                                                               :aggregations {:builder
                                                                              {:terms {:field "build.builder"}}}})
        buckets (-> result :aggregations :builder :buckets)]
    (log/info "build-tool-buckets" result)
    (log/info "buckets" buckets)
    (response {:buckets buckets})))

(defn get-snapshot-by-id
  [id]
  (response (:_source (store/get-document stores/snapshots-store id))))

(defn put-snapshot!
  [id snapshot]
  (response (store/put-document snapshot stores/snapshots-store id)))

