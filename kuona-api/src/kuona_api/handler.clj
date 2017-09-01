(ns kuona-api.handler
  (:require [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [kuona-api.environments :refer :all]
            [kuona-core.metric.store :as store]
            [kuona-core.util :as util]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [resource-response response status]])
  (:import java.util.Properties)
  (:gen-class))

(defn service-data
  []
  (response { :links [{:href "/api/environments" :rel "environments"}]}))

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
(def commits (store/mapping :commits (store/index :kuona-repositories "http://localhost:9200")))

(def snapshots (store/mapping :snapshots (store/index :kuona-snapshots "http://localhost:9200")))

(defn repository-page-link
  [page-number]
  (str "/api/repositories?page=" page-number))

(defn page-link
  [base page-number]
  (str base "?page=" page-number))

(defn- get-repository-count [] (response (store/get-count repositories)))

(defn- get-repositories
  [search page]
  (log/info "get repositories" search page)
  (response (store/search repositories search 100 page repository-page-link)))

(defn- build-tool-buckets
  []
  (log/info "build-tool-buckets")
  (let [result  (store/internal-search snapshots { :size        0
                                                  :aggregations {:builder { :terms { :field "build.builder" }}}})
        buckets (-> result :aggregations :builder :buckets)]
    (log/info "build-tool-buckets" result)
    (log/info "buckets" buckets)
    (response {:buckets buckets})))

(defn bad-request
  [error]
  {:status  400
   :headers {"Content-Type" "application-json"}
   :body    (generate-string {:error error})})

(defn put-commit!
  [id commit]
  (log/info "new commit for repository " id)
  (let [commit-id (-> commit :id)
        entity (merge commit {:repository_id id})]
    (cond
      (nil? commit-id) (bad-request "malformed request - missing commit identity")
      :else            (response (store/put-document entity commits commit-id)))))

(defn commits-page-link
  [id page-number]
  (str "/api/repositories/" id "/commits?page=" page-number))

(defn get-commits
  [id page]
  (log/info "finding commits for repository " id " page " page)
  (response (store/search commits (str "repository_id:" id) 100 page #(commits-page-link id %))))

(defn get-repository-by-id
  [id]
  (response (:_source (store/get-document repositories id))))

(defn put-repository!
  [id repo]
  (response (store/put-document repo repositories id)))

(defn get-snapshot-by-id
  [id]
  (response (:_source (store/get-document snapshots id))))

(defn put-snapshot!
  [id snapshot]
  (response (store/put-document snapshot snapshots id)))

(defn get-metrics
  [mapping, search page]
  (response (store/search (store/mapping mapping kuona-metrics-index) search 100 page #(page-link (str "/api/mapping/" mapping) %))))

(defn get-metrics-count
  [mapping]
  (response (store/get-count (store/mapping mapping kuona-metrics-index))))

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


(defn get-version [dep]
  (let [path (str "META-INF/maven/" (or (namespace dep) (name dep))
                  "/" (name dep) "/pom.properties")
        props (io/resource path)]
    (when props
      (with-open [stream (io/input-stream props)]
        (let [props (doto (Properties.) (.load stream))]
          (.getProperty props "version"))))))

(defn get-api-info
  []
  (let [es (util/parse-json-body (http/get "http://localhost:9200"))]
    (response {:kuona_api      {:version (get-version 'kuona-api)}
               :clojure        {:version (get-version 'org.clojure/clojure)}
               :elastic_search es})))

(defroutes app-routes
  (GET "/" [] (service-data))
  (GET "/api/repositories/count" [] (get-repository-count))
  (GET "/api/repositories" [search page] (get-repositories search page))
  (GET "/api/repositories/:id" [id] (get-repository-by-id id))
  (PUT "/api/repositories/:id" request (put-repository! (get-in request [:params :id])  (get-in request [:body])))

  (GET "/api/repositories/:id/commits" request (get-commits (get-in request [:params :id]) 1))
  (PUT "/api/repositories/:id/commits" request (put-commit! (get-in request [:params :id]) (get-in request [:body])))

  (GET "/api/build/tools" [] (build-tool-buckets))

  (GET "/api/snapshots/:id" [id] (get-snapshot-by-id id))
  (PUT "/api/snapshots/:id" request (put-snapshot! (get-in request [:params :id]) (get-in request [:body])))

  (GET "/api/metrics/:mapping" [mapping search page] (get-metrics mapping search page))
  (GET "/api/metrics/:mapping/count" [mapping] (get-metrics-count mapping))

  (GET "/api/environments" [] (get-environments))
  (GET "/api/environments/:id" [id] (get-environment-by-id id))
  (GET "/api/environments/:id/comments" request (get-environment-comments (get-in request [:params :id])))
  (POST "/api/environments/:id/comments" request (put-environment-comment! (get-in request [:params :id]) (get-in request [:body :comment])))
  (POST "/api/environments/:id/version" request (put-environment-version! (get-in request [:params :id]) (get-in request [:body :version])))
  (POST "/api/environments/:id/status" request (put-environment-status! (get-in request [:params :id]) (get-in request [:body :status])))
  (POST "/api/environments" request (put-environment! (get-in request [:body :environment])))

  (GET "/api/valuestreams" [] (get-value-streams))
  (GET "/api/valuestreams/:id" [id] (get-value-stream id))
  (GET "/api/info" [] (get-api-info))

  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))
