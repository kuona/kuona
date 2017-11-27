(ns kuona-api.handler
  (:require [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [clj-http.client :as http]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [kuona-api.environments :refer :all]
            [kuona-api.environment-handlers :as environments]
            [kuona-core.metric.store :as store]
            [kuona-core.util :as util]
            [kuona-api.valuestream-handlers :as valuestream]
            [kuona-api.repository-handlers :as repository]
            [kuona-api.metric-handlers :as metric-handlers]
            [kuona-api.snapshot-handlers :as snap-handlers]
            [kuona-api.collector-handlers :as collectors]
            [kuona-api.build-handlers :as build]
            [kuona-api.query-handlers :as query]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [resource-response response status]])
  (:gen-class))



(defn service-data []
  {:links [{:href "/api/environments" :rel "environments"}
           {:href "/api/repositories" :rel "repositories"}
           {:href "/api/build" :rel "build"}
           {:href "/api/snapshots" :rel "snapshots"}
           {:href "/api/metrics" :rel "metrics"}
           {:href "/api/valuestreams" :rel "valuestreams"}
           {:href "/api/info" :rel "info"}]})

(defn get-service-data
  []
  (response (service-data)))

(defn api-info
  []
  (let [es (util/parse-json-body (http/get "http://localhost:9200"))]
    {:kuona_api      {:version (util/get-project-version 'kuona-api)}
     :clojure        {:version (util/get-project-version 'org.clojure/clojure)}
     :elastic_search es}))

(defn get-api-info
  []
  (let [es (util/parse-json-body (http/get "http://localhost:9200"))]
    (response (merge (api-info) (service-data)))))

(defroutes app-routes
           (GET "/api" [] (get-service-data))
           (GET "/api/info" [] (get-api-info))

           (GET "/api/repositories/count" [] (repository/get-repository-count))
           (GET "/api/repositories" [search page] (repository/get-repositories search page))
           (GET "/api/repositories/:id" [id] (repository/get-repository-by-id id))
           (PUT "/api/repositories/:id" request (repository/put-repository! (get-in request [:params :id]) (get-in request [:body])))

           (GET "/api/repositories/:id/commits" request (repository/get-commits (get-in request [:params :id]) 1))
           (PUT "/api/repositories/:id/commits" request (repository/put-commit! (get-in request [:params :id]) (get-in request [:body])))

           (GET "/api/build/tools" [] (snap-handlers/build-tool-buckets))

           (GET "/api/snapshots/:id" [id] (snap-handlers/get-snapshot-by-id id))
           (PUT "/api/snapshots/:id" request (snap-handlers/put-snapshot! (get-in request [:params :id]) (get-in request [:body])))

           (GET "/api/metrics/:mapping" [mapping search page] (metric-handlers/get-metrics mapping search page))
           (GET "/api/metrics/:mapping/count" [mapping] (metric-handlers/get-metrics-count mapping))

           (GET "/api/environments" [] (environments/get-environments))
           (GET "/api/environments/:id" [id] (environments/get-environment-by-id id))
           (GET "/api/environments/:id/comments" request (environments/get-environment-comments (get-in request [:params :id])))
           (POST "/api/environments/:id/comments" request (environments/put-environment-comment! (get-in request [:params :id]) (get-in request [:body :comment])))
           (POST "/api/environments/:id/version" request (environments/put-environment-version! (get-in request [:params :id]) (get-in request [:body :version])))
           (POST "/api/environments/:id/status" request (environments/put-environment-status! (get-in request [:params :id]) (get-in request [:body :status])))
           (POST "/api/environments" request (environments/put-environment! (get-in request [:body :environment])))

           (GET "/api/valuestreams" [] (valuestream/get-value-streams))
           (GET "/api/valuestreams/:id" [id] (valuestream/get-value-stream id))

           (POST "/api/collectors/activities" request (collectors/put-activity! (get-in request [:body])))
           (GET "/api/collectors/activities" [] (collectors/get-activities))

           (POST "/api/builds" request (build/put-build! (get-in request [:body])))

           (GET "/api/query" [] (query/get-sources))
           (POST "/api/query/:source" request (query/query-source (get-in request [:params :source]) (get-in request [:body])))

           (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))
