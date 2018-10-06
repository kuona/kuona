(ns kuona-api.handler
  (:require [cheshire.core :refer :all]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [kuona-api.environments :refer :all]
            [kuona-api.environments :refer :all]
            [kuona-api.environment-handlers :as environments]
            [kuona-core.store :as store]
            [kuona-core.stores :as stores]
            [kuona-core.util :as util]
            [kuona-core.workspace :as workspace]
            [kuona-api.valuestream-handlers :as valuestream]
            [kuona-api.repository-handlers :as repository]
            [kuona-api.metric-handlers :as metric-handlers]
            [kuona-api.snapshot-handlers :as snap-handlers]
            [kuona-api.collector-handlers :as collectors]
            [kuona-api.build-handlers :as build]
            [kuona-api.query-handlers :as query]
            [kuona-api.dashboard-handlers :as dashboards]
            [kuona-api.integration-handlers :as integration]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [file-response resource-response response status redirect]]
            [clojure.java.io :as io]
            [clj-http.client :as http])
  (:gen-class))



(defn service-data []
  {:links [{:href "/api/environments" :rel "environments"}
           {:href "/api/repositories" :rel "repositories"}
           {:href "/api/build" :rel "build"}
           {:href "/api/snapshots" :rel "snapshots"}
           {:href "/api/metrics" :rel "metrics"}
           {:href "/api/valuestreams" :rel "valuestreams"}
           {:href "/api/info" :rel "info"}
           {:href "/api/query" :rel "query"}]})

(defn get-service-data
  []
  (response (service-data)))

(defn api-info
  []
  (let [es (kuona-core.http/json-get (stores/es-url))]
    {:kuona_api      {:version (util/get-project-version 'kuona-api)}
     :clojure        {:version (util/get-project-version 'org.clojure/clojure)}
     :elastic_search es}))

(defn get-api-info
  []
  (response
    (merge (api-info)
           (service-data))))

(defn get-api-status
  []
  (response {:status :ok
             :store  {:health (store/health)}}))

(defroutes app-routes
           (GET "/" [] (io/resource "public/index.html"))
           (route/resources "/")
           (GET "/api" [] (get-service-data))
           (GET "/api/info" [] (get-api-info))
           (GET "/api/status" [] (get-api-status))

           (GET "/api/repositories/count" [] (repository/get-repository-count))
           (GET "/api/repositories" [search page] (repository/get-repositories search page))
           (POST "/api/repositories" request (repository/add-repository (get-in request [:body])))
           (GET "/api/repositories/:id" [id] (repository/get-repository-by-id id))
           (PUT "/api/repositories/:id" request (repository/put-repository! (get-in request [:body]) (get-in request [:params :id])))
           (GET "/api/repositories/:id/commits" request (repository/get-commits (get-in request [:params :id]) 1))
           (PUT "/api/repositories/:id/commits" request (repository/put-commit! (get-in request [:params :id]) (get-in request [:body])))
           (POST "/api/repositories/test" request (repository/test-project-url (get-in request [:body])))
           (GET "/api/repositories/manifest/:id" [id] (file-response id {:root (workspace/get-workspace-path)}))

           (POST "/api/health-checks" request (route/not-found "Not yet available"))
           (GET "/api/health-checks/logs" request (route/not-found "Not yet available"))
           (GET "/api/health-checks/:id/" request (route/not-found "Not yet available"))
           (GET "/api/health-checks/:id/status" request (route/not-found "Not yet available"))
           (GET "/api/health-checks/:id/logs" request (route/not-found "Not yet available"))

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

           (POST "/api/collectors" request (collectors/put-collector! (get-in request [:body])))
           (GET "/api/collectors" [collector-type] (collectors/collector-list collector-type))
           (DELETE "/api/collectors/:id" [id] (collectors/delete-collector! id))

           (POST "/api/builds" request (build/put-build! (get-in request [:body])))

           (GET "/api/dashboards" [search page] (dashboards/search search page))
           (POST "/api/dashboards" request (dashboards/put! (get-in request [:body])))
           (GET "/api/dashboards/:id" [id] (dashboards/get-by-id id))

           (GET "/api/query" [] (redirect "/api/query/sources"))
           (GET "/api/query/sources" [] (query/get-sources))
           (POST "/api/query/:source" request (query/query-source (get-in request [:params :source]) (get-in request [:body])))
           (GET "/api/query/:source/schema" [source] (query/source-schema source))

           (GET "/api/indices" [] (response (store/indices)))
           (POST "/api/indices/:id/rebuild" [id] (response (stores/rebuild-store-by-name id)))
           (POST "/api/indices/:id/unlock" [id] (response (stores/unlock-store-by-name id)))
           (DELETE "/api/indicies/:id" [id] (response (stores/delete-index-by-id id)))

           (POST "/api/integration/test" request (integration/test-integration (get-in request [:body])))
           (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))
