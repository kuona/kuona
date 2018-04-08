(ns kuona-api.collector-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-core.metric.store :as store]
            [ring.util.response :refer [resource-response response status]]
            [kuona-core.util :as util]
            [kuona-core.elasticsearch :as es])
  (:gen-class))

(def keyword-field
  {:type "text" :fields {:keyword {:type "keyword" :ignore_above 256}}})

(def mapping
  {:activity {:properties {:activity  {:type   "text",
                                       :fields {:keyword {:type "keyword", :ignore_above 256}}},
                           :collector {:properties {:name    {:type "text", :fields {:keyword {:type "keyword", :ignore_above 256}}},
                                                    :version {:type "text", :fields {:keyword {:type "keyword", :ignore_above 256}}}}},
                           :id        {:type "text", :fields {:keyword {:type "keyword", :ignore_above 256}}},
                           :timestamp {:type "date"}}}})

(def collector-mapping
  {:collector {:properties
               {:collector es/string-not-analyzed
                :config    {:properties {:url      es/string
                                         :username es/string
                                         :password es/string-not-analyzed}}}}})

(def collectors (store/mapping :activity (store/index :kuona-collectors "http://localhost:9200")))

(def collector-config (store/mapping :collector (store/index :kuona-collector-config "http://localhost:9200")))

(defn create-collectors-index-if-missing
  []
  (let [index (store/index "kuona-collectors" "http://localhost:9200")
        collector-index (store/index "kuona-collectors" "http://localhost:9200")]
    (if (store/has-index? index) nil (store/create-index index mapping))
    (if (store/has-index? collector-index) nil (store/create-index collector-index collector-mapping))))

(defn put-activity!
  [data]
  (let [id (:id data)]
    (log/info "New collector activity" id (-> data :collector :name) (-> data :collector :version))
    (response (store/put-document data collectors id))))

(defn page-link
  [base page-number]
  (str base "?page=" page-number))

(defn get-activities
  []
  (let [url (str collectors "/_search?size=100&sort=timestamp:desc")]
    (response (store/find-documents url))))

(defn put-collector!
  "Stores a collector configuration"
  [c]
  (let [id (util/uuid)
        doc (merge {:id id} c)]
    (log/info "Adding collector document" doc)
    (response (store/put-document doc collector-config id))))

(defn delete-collector!
  [id]
  (log/info "Deleting collector with id" id)
  (response (store/delete-document collector-config id)))

(defn collector-list
  "Reads the list of defined collectors"
  ([]
   (let [url (str collector-config "/_search?size=100")]
     (response (store/find-documents url))))

  ([collector-type]
   (if (nil? collector-type)
     (collector-list)
     (let [url (str collector-config "/_search?size=100&q=collector_type:" collector-type)]
       (response (store/find-documents url))))))
