(ns kuona-api.health-check-handlers
  (:require [ring.util.response :as response]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer :all]
            [kuona-api.core.store :as store]
            [kuona-api.core.stores :as stores]
            [kuona-api.core.util :as util]
            [kuona-api.core.healthcheck :as health-check])
  (:gen-class))


(defn new-health-check
  [health-check]
  (let [status (health-check/valid-health-check? health-check)]
    (log/info "New healthcheck " health-check)
    (if (-> status :valid)
      (response/created (store/put-document health-check stores/health-check-store (util/uuid)))
      (response/status (response/response status) 401))))

(defn find-health-checks [params]
  (response/response {:health_checks (store/all-documents stores/health-check-store)}))

(defn find-health-check-logs [param]
  (response/response {:health_checks (store/all-documents stores/health-check-log-store)}))

(defn delete-by-id [id]
  (response/response (store/delete-document stores/health-check-store id)))

(defn find-health-check-snapshots []
  (response/response (store/all-documents stores/health-check-snapshot-store)))
