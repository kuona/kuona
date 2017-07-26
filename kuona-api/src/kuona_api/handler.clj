(ns kuona-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :refer [resource-response response status]]
            [ring.middleware.json :as middleware]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.adapter.jetty :as jetty]
            [kuona-api.environments :refer :all]
            [kuona-core.metric.store :as store])
  (:gen-class))


(def service-data
  { :links [{:href "/api/environments" :rel "environments"}]})

(defn environment-links
  [e]
  [{:href (str "/api/environments/" (:name e)) :rel :self}
   {:href (str "/api/environments/" (:name e) "/comments") :rel :comments}])

(defn decorate-environment
  "Adds environment and links to the supplied map"
  [e]
    { :environment e :links (environment-links e)})

(defn environment-link-decorate-environment-list
  [environments]
  (map decorate-environment environments))

(defn decorate-response
  [decorator value]
  (if value
    (status (response (decorator value)) 200)
    (status (response {}) 404)))

(def kuona-metrics-index (store/index :kuona-metrics "http://localhost:9200"))

(def environments (store/mapping :environments (store/index :kuona-env "http://localhost:9200")))
(def environment-comments (store/mapping :comments (store/index :kuona-env "http://localhost:9200")))

(def repositories (store/mapping :repositories (store/index :kuona-repositories "http://localhost:9200")))

(def snapshots (store/mapping :snapshots (store/index :kuona-snapshots "http://localhost:9200")))

(defn repository-page-link
  [page-number]
  (str "/api/repositories?page=" page-number))

(defn page-link
  [base page-number]
  (str base "?page=" page-number))

(defn get-repository-count [] (store/get-count repositories))

(defn get-repositories
  [search page]
  (response (store/search repositories search 100 page repository-page-link)))

(defroutes app-routes
  (GET "/" [] (response service-data))
  (GET "/api/repositories/count" [] (response (get-repository-count)))
  (GET "/api/repositories" [search page] (get-repositories search page))
  (GET "/api/repositories/:id" [id] (response (:_source (store/get-document repositories id))))
  (PUT "/api/repositories/:id" request (response (store/put-document (get-in request [:body]) repositories (get-in request [:params :id]))))

  (GET "/api/snapshots/:id" [id] (response (:_source (store/get-document snapshots id))))
  (PUT "/api/snapshots/:id" request (response (store/put-document (get-in request [:body]) snapshots (get-in request [:params :id]))))

  (GET "/api/metrics/:mapping" [mapping search page] (response (store/search (store/mapping mapping kuona-metrics-index) search 100 page #(page-link (str "/api/mapping/" mapping) %))))
  (GET "/api/metrics/:mapping/count" [mapping] (response (store/get-count (store/mapping mapping kuona-metrics-index))))
  (GET "/api/environments" [] (response { :environments (environment-link-decorate-environment-list (store/all-documents environments)) }))
  (GET "/api/environments/:id" [id] (decorate-response decorate-environment (store/get-document environments id)))
  (POST "/api/environments/:id/comments" request (response
                                                  (decorate-environment
                                                   (put-comment environments environment-comments (get-in request [:params :id]) (get-in request [:body :comment])))))
  (POST "/api/environments/:id/version" request (response
                                                  (decorate-environment
                                                   (put-version environments (get-in request [:params :id]) (get-in request [:body :version])))))
  (POST "/api/environments/:id/status" request (response
                                                (decorate-environment
                                                 (put-status environments (get-in request [:params :id]) (get-in request [:body :status])))))
  (GET "/api/environments/:id/comments" request (response (get-comments environment-comments (get-in request [:params :id]))))
  (POST "/api/environments" request (response (decorate-environment (store/put-document environments (get-in request [:body :environment])))))
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))
