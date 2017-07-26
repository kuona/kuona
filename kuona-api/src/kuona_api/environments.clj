(ns kuona-api.environments
  (:require [slingshot.slingshot :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.util :refer :all]
            [kuona-core.metric.store :as store]
            [kuona-api.mapping :refer :all])
  (:gen-class))

;(defn create-index
;  "Creates the currently configured index if it does not already
;  exist."
;  []
;  (try+
;   (println "Creating index" @index)
;   (esi/create conn @index)
;   (catch [:status 400] {:keys [request-time headers body]}
;     (println "Index already exists"))
;   (catch Object _
;     (log/error (:throwable &throw-context) "unexpected error")
;     (throw+))))


;(defn update-environment-mapping
;  "Updates the currently configured mapping in elasticsearch to match
;  the current mapping requirements"
;  []
;  (log/info "Updating " @index " mappings")
;
;  (try+
;   (esi/open conn @index)
;   (catch [:status 404] {:keys [request-time headers body]}
;     (println "Index does not exist")
;     (create-index))
;   (catch Object _
;     (log/error (:throwable &throw-context) "unexpected error")
;     (throw+)))
;
;  (let [comment-result (esi/update-mapping conn @index "comments" :mapping comment-mapping-type )
;        environment-result (esi/update-mapping conn @index "environments" :mapping environment-mapping-type)]
;    (log/info "Comment mapping updated " comment-result)
;    (log/info "Environment mapping updated " environment-result)
;    (conj comment-result environment-result)))

;(defn set-and-initialize-index
;  "Sets the current index name and then ensures that the index is
;  useable by creating it if it does not exist and then updating or
;  setting the mapping for the index."
;  [name]
;  (set-environment-index name)
;  (update-environment-mapping))

(defn put-comment
  "Add a comment to the environment referenced by the id"
  [env-mapping comment-mapping id comment]
  (log/info "Adding comment to " id comment)
  (let [comment-id (uuid)
        comment-entry {:assessment (:assessment comment) :message (:message comment) :username (:username comment) :timestamp (timestamp) :tags ["ENVIRONMENT" id]}
        entry (store/get-document env-mapping id)
        updated-entry (assoc entry :comment comment-entry)]
    (store/put-document env-mapping updated-entry)
    (store/put-document comment-mapping comment-id comment-entry)
    updated-entry))

(defn put-version
  "Set the version label of the specified environment"
  [mapping id version]
  (log/info "Setting the version to " id version)
  (store/put-partial-document mapping id {:version version})
  (store/get-document mapping id))

(defn put-status
  "Set the version label of the specified environment"
  [mapping id status]
  (log/info "Setting the satus to " id status)
  (store/put-partial-document mapping id {:status status})
  (store/get-document mapping id))


(defn get-comments
  "Returns all the comments based on supplied environemnt id"
  [mapping environment-id]
  [])
;  (let [res  (esd/search conn @index "comments" :query  (q/filtered :query  {:and [(q/term :tags environment-id) (q/term :tags "ENVIRONMENT")]}))
;        n    (esrsp/total-hits res)
;        hits (esrsp/hits-from res)]
;    (log/info "Query for all environments returned " res)
;    (map :_source hits)))
