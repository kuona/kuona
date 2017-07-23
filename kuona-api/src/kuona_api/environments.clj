(ns kuona-api.environments
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.index    :as esi]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [slingshot.slingshot :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.util :refer :all]
            [kuona-api.mapping :refer :all])
  (:gen-class))

(def index (volatile! "kuona-env"))

(defn set-environment-index
  "Changes the index used by the application. All future connections
  and operations will work of the specified index"
  [name]
  (vreset! index name))

(def mapping "environments")

(defn connect
  "Returns a connection based on the supplied configuration"
  [config]
  (esr/connect config))

(def conn
  (connect "http://localhost:9200"))


(defn create-index
  "Creates the currently configured index if it does not already
  exist."
  []
  (try+
   (println "Creating index" @index)
   (esi/create conn @index)
   (catch [:status 400] {:keys [request-time headers body]}
     (println "Index already exists"))
   (catch Object _
     (log/error (:throwable &throw-context) "unexpected error")
     (throw+))))


(defn update-environment-mapping
  "Updates the currently configured mapping in elasticsearch to match
  the current mapping requirements"
  []
  (log/info "Updating " @index " mappings")

  (try+
   (esi/open conn @index)
   (catch [:status 404] {:keys [request-time headers body]}
     (println "Index does not exist")
     (create-index))
   (catch Object _
     (log/error (:throwable &throw-context) "unexpected error")
     (throw+)))

  (let [comment-result (esi/update-mapping conn @index "comments" :mapping comment-mapping-type )
        environment-result (esi/update-mapping conn @index "environments" :mapping environment-mapping-type)]
    (log/info "Comment mapping updated " comment-result)
    (log/info "Environment mapping updated " environment-result)
    (conj comment-result environment-result)))

(defn set-and-initialize-index
  "Sets the current index name and then ensures that the index is
  useable by creating it if it does not exist and then updating or
  setting the mapping for the index."
  [name]
  (set-environment-index name)
  (update-environment-mapping))

(defn list-environments
  "List all available evnironments defined in the store"
  []
  (let [res  (esd/search conn @index mapping)
        n    (esrsp/total-hits res)
        hits (esrsp/hits-from res)]
    (log/info "Query for all environments returned " res)
    (map :_source hits)))

(defn get-environment
  "Get details of a single environemnt by id"
  [id]
  (let [result (esd/get conn @index mapping id)] ;; fetch a single document by a known id
    (log/info "read environment " id " returned " result)
    (:_source result)))

(defn put-environment
  "Update or create a single environment. The ID is read from the :name attribute of the supplied data parameter"
  [env]
  (log/info "New environment request " env)
  (let [id (:name env)
        result (esd/put conn @index mapping id env)]
    (get-environment id)))

(defn put-comment
  "Add a comment to the environment referenced by the id"
  [id comment]
  (log/info "Adding comment to " id comment)
  (let [comment-id (uuid)
        comment-entry {:assessment (:assessment comment) :message (:message comment) :username (:username comment) :timestamp (timestamp) :tags ["ENVIRONMENT" id]}
        entry (get-environment id)
        updated-entry (assoc entry :comment comment-entry)]
    (put-environment updated-entry)
    (esd/put conn @index "comments" comment-id comment-entry)
    updated-entry))

(defn put-version
  "Set the version label of the specified environment"
  [id version]
  (log/info "Setting the version to " id version)
  (esd/update-with-partial-doc conn @index mapping id {:version version})
  (get-environment id))

(defn put-status
  "Set the version label of the specified environment"
  [id status]
  (log/info "Setting the satus to " id status)
  (esd/update-with-partial-doc conn @index mapping id {:status status})
  (get-environment id))


(defn get-comments
  "Returns all the comments based on supplied environemnt id"
  [environment-id]
  (let [res  (esd/search conn @index "comments" :query  (q/filtered :query  {:and [(q/term :tags environment-id) (q/term :tags "ENVIRONMENT")]}))
        n    (esrsp/total-hits res)
        hits (esrsp/hits-from res)]
    (log/info "Query for all environments returned " res)
    (map :_source hits)))
