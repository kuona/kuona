(ns kuona-api.snapshot-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-api.environments :refer :all]
            [kuona-core.metric.store :as store]
            [kuona-core.util :as util]
            [ring.util.response :refer [resource-response response status]])
  (:gen-class))

(def snapshots (store/mapping :snapshots (store/index :kuona-snapshots "http://localhost:9200")))


(defn build-tool-buckets
  []
  (log/info "build-tool-buckets")
  (let [result  (store/internal-search snapshots { :size        0
                                                  :aggregations {:builder { :terms { :field "build.builder" }}}})
        buckets (-> result :aggregations :builder :buckets)]
    (log/info "build-tool-buckets" result)
    (log/info "buckets" buckets)
    (response {:buckets buckets})))

(defn get-snapshot-by-id
  [id]
  (response (:_source (store/get-document snapshots id))))

(defn put-snapshot!
  [id snapshot]
  (response (store/put-document snapshot snapshots id)))

