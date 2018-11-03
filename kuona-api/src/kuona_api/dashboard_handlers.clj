(ns kuona-api.dashboard-handlers
  (:require [clojure.tools.logging :as log]
            [ring.util.response :refer [response]]
            [kuona-api.core.store :as store]
            [kuona-api.core.stores :as stores]
            [kuona-api.core.util :as util]))

(defn all-dashboards
  [search page]
  (log/info "get dashboards" search page)
  (response {:dashboards (store/all-documents stores/dashboards-store)}))

(defn put!
  ([dashboard] (put! dashboard (:name dashboard)))
  ([dashboard id]
   (response (store/put-document
               (merge dashboard {:created (util/timestamp)})
               stores/dashboards-store
               id))))

(defn get-by-id [id]
  [id]
  (response {:dashboard (:_source (store/get-document stores/dashboards-store id))}))

(defn delete-dashboard
  [id]
  (response (:_source (store/delete-document stores/dashboards-store id))))
