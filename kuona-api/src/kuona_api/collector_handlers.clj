(ns kuona-api.collector-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-core.metric.store :as store]
            [kuona-core.util :as util]
            [ring.util.response :refer [resource-response response status]])
  (:gen-class))

(def keyword-field
  {:type "text" :fields {:keyword {:type "keyword" :ignore_above 256}}})

(def mapping
  {:activity {:properties {:activity {:type "text", :fields {:keyword {:type "keyword", :ignore_above 256}}},
                           :collector {:properties {:name {:type "text", :fields {:keyword {:type "keyword", :ignore_above 256}}},
                                                    :version {:type "text", :fields {:keyword {:type "keyword", :ignore_above 256}}}}},
                           :id {:type "text", :fields {:keyword {:type "keyword", :ignore_above 256}}},
                           :timestamp {:type "date"}}}})

(def collectors (store/mapping :activity (store/index :kuona-collectors "http://localhost:9200")))

(defn create-collectors-index-if-missing
  []
  (let [index (store/index "kuona-collectors" "http://localhost:9200")]
    (if (store/has-index? index) nil (store/create-index index mapping))))



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
    (response (store/find url))))
