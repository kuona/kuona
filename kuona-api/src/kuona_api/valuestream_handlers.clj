(ns kuona-api.valuestream-handlers
  (:require [cheshire.core :refer :all]
            [compojure.core :refer :all]
            [kuona-api.environments :refer :all]
            [kuona-core.util :as util]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.util.response :refer [resource-response response status]])
  (:gen-class))


(defn make-commit
  []
  {
   :id            (util/uuid)
   :email         "graham@grahambrooks.com"
   :time          "2014-08-13T14:11:41Z"
   :branches      [ "refs/heads/master" ]
   :changed_files [ [ "some/path" "edit" ] ]
   :merge         false
   :author        "Graham Brooks",
   :repository_id (util/uuid)
   :message       "A message" })




(defn value-stream
  []
  {
   :id          (util/uuid)
   :name        "unique-name"
   :timestamp   (util/timestamp)
   :artifact    {:id "some-unique-identifier" :version "1.0.1"}
   :lead_time   91237842
   :commits     [(make-commit)
                 (make-commit)]
   :build       {:timestamp (util/timestamp)
                 :duration  234234
                 :builder   "Jenkins"
                 :build_url "http://jenkins.com/stage/job"
                 }
   :deployments [{:timestamp   (util/timestamp)
                  :environment {:name "PROD"}
                  :duration    234523}]
   })

(defn get-value-streams
  "Returns a list of currently defined value streams"
  []
  (response {:valuestreams [(value-stream)
                            (value-stream)
                            (value-stream)]}))

(defn get-value-stream
  "Returns a single valuestream object identified by the supplied id"
  [id]
  (response (value-stream)))
