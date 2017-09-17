(ns kuona-api.build-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-core.metric.store :as store]
            [kuona-core.util :as util]
            [ring.util.response :refer [resource-response response status]])
  (:gen-class))


(def build-mapping (store/mapping :builds (store/index :kuona-builds "http://localhost:9200")))


(defn put-build!
  [data]
  (let [id (-> data :build :id)]
    (log/info "put-build!" id (-> data :build :name) (-> data :build :number))
    (response (store/put-document data build-mapping id))))
