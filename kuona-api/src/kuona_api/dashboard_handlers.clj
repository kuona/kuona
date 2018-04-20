(ns kuona-api.dashboard-handlers
  (:require [clojure.tools.logging :as log]
            [ring.util.response :refer [response]]
            [kuona-core.store :as store]
            [kuona-core.stores :as stores]))

(defn- dashboards-page-link
  [page-number]
  (str "/api/repositories?page=" page-number))


(defn list
  [search page]
  (log/info "get repositories" search page)
  (response (store/search stores/dashboards-store search 100 page dashboards-page-link)))

(defn put!
  ([dashboard] (put! dashboard (:name dashboard)))
  ([dashboard id]
   (response (store/put-document dashboard stores/dashboards-store id))))

(defn get-by-id [id]
  [id]
  (response (:_source (store/get-document stores/dashboards-store id))))
