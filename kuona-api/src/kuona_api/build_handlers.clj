(ns kuona-api.build-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-core.metric.store :as store]
            [ring.util.response :refer [resource-response response status]]
            [kuona-core.stores :refer [builds-store]])
  (:gen-class))



(defn put-build!
  [data]
  (let [build (-> data :build)
        id    (-> build :id)]
    (log/info "put-build!" id (-> build :name) (-> build :number))
    (response (store/put-document build builds-store id))))
