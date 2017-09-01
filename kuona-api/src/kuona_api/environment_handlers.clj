(ns kuona-api.environment-handlers
  (:require [clojure.tools.logging :as log]
            [kuona-api.environments :refer :all]
            [kuona-core.metric.store :as store]
            [kuona-core.util :as util]
            [ring.util.response :refer [resource-response response status]])
  (:gen-class))

(def environments (store/mapping :environments (store/index :kuona-env "http://localhost:9200")))
(def environment-comments (store/mapping :comments (store/index :kuona-env "http://localhost:9200")))


(defn environment-links
  [e]
  [{:href (str "/api/environments/" (:name e)) :rel :self}
   {:href (str "/api/environments/" (:name e) "/comments") :rel :comments}])

(defn decorate-environment
  "Adds environment and links to the supplied map"
  [e]
    { :environment e :links (environment-links e)})

(defn decorate-response
  [decorator value]
  (if value
    (status (response (decorator value)) 200)
    (status (response {}) 404)))


(defn environment-link-decorate-environment-list
  [environments]
  (map decorate-environment environments))

(defn get-environments
  []
  (response { :environments (environment-link-decorate-environment-list (store/all-documents environments)) }))

(defn get-environment-by-id
  [id]
  (decorate-response decorate-environment (store/get-document environments id)))

(defn get-environment-comments
  [id]
  (response (get-comments environment-comments id)))

(defn put-environment-comment!
  [id comment]
  (response
   (decorate-environment (put-comment environments environment-comments id comment))))

(defn put-environment-version!
  [id version]
  (response  (decorate-environment (put-version environments id version))))

(defn put-environment-status!
  [id status]
  (response (decorate-environment (put-status environments id status))))

(defn put-environment!
  [environment]
  (response (decorate-environment (store/put-document environments environment))))
