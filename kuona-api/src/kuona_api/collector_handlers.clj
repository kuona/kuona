(ns kuona-api.collector-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-core.store :as store]
            [ring.util.response :refer [resource-response response status]]
            [kuona-core.util :as util]
            [kuona-core.elasticsearch :as es]
            [kuona-core.stores :refer [collector-activity-store collector-config-store]])
  (:gen-class))


(defn put-activity!
  [data]
  (let [id (:id data)]
    (log/info "New collector activity" id (-> data :collector :name) (-> data :collector :version))
    (response (store/put-document data collector-activity-store id))))

(defn page-link
  [base page-number]
  (str base "?page=" page-number))

(defn get-activities
  []
  (let [url (.url collector-activity-store ["_search"] ["size=100" "sort=timestamp:desc"])]
    (response (store/find-documents url))))

(defn put-collector!
  "Stores a collector configuration"
  [c]
  (let [id  (util/uuid)
        doc (merge {:id id} c)]
    (log/info "Adding collector document" doc)
    (response (store/put-document doc collector-config-store id))))

(defn delete-collector!
  [id]
  (log/info "Deleting collector with id" id)
  (response (store/delete-document collector-config-store id)))

(defn collector-list
  "Reads the list of defined collectors"
  ([]
   (let [url (.url collector-config-store ["_search"] ["size=100"])]
     (response (store/find-documents url))))

  ([collector-type]
   (if (nil? collector-type)
     (collector-list)
     (let [qry (str "q=collector_type:" collector-type)
           url (.url collector-config-store ["_search"] ["size=100" qry])]
       (response (store/find-documents url))))))
