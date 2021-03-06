(ns kuona-api.core.store
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http-client]
            [kuona-api.core.http :as http]
            [clojure.tools.logging :as log]
            [kuona-api.core.util :refer :all]
            [slingshot.slingshot :refer :all]
            [kuona-api.core.stores :refer []]
            [kuona-api.core.stores :as stores])
  (:gen-class)
  (:import (kuona_api.core.stores DataStore)))

(defn health
  []
  (let [response (http-client/get (stores/es-url "_cluster" "health"))]
    (http/parse-json-body response)))

(defn put-document
  ([document ^DataStore store] (put-document document store (uuid)))
  ([document ^DataStore store id]
   (let [url (.url store [id])]
     (log/info "put-document " id url)
     (http/json-put url document))))

(defn put-partial-document
  [^DataStore store id update]
  (let [url     (.url store '(id "_update"))
        request {:doc update}]
    (log/info "put-partial-update " store id)
    (http/json-post url request)))

(defn get-count
  [^DataStore store]
  (let [url (.url store ["_count"])]
    (log/info "Reading document count for " url)
    (http/json-get url)))

(defn page-links
  [f & {:keys [size count]}]
  (cond
    (<= count 0) []
    (<= size 0) []
    :else
    (let [pages (/ count size)]
      (into [] (map f (range 1 (+ pages 1)))))))

(defn parse-integer
  [n]
  (cond
    (= (type n) Long) n
    :else (try+ (. Integer parseInt n)
                (catch Object _ nil))))


(defn pagination-param
  [& {:keys [:size :page]}]
  (let [page-number (parse-integer page)]
    (cond
      (nil? page-number) (str "size=" size)
      (= page-number 1) (str "size=" size)
      :else (str "size=" size "&" "from=" (* (- page-number 1) size)))))

(defn internal-search
  [^DataStore store query]
  (log/info "internal-search" (-> store :index-name))
  (let [url (.url store '("_search"))]
    (http/json-post url query)))

(defn source-with-id
  "Mergese the _id field with the contents of the _source object in results"
  [r] (merge {:id (:_id r)} (:_source r)))


(defn search2
  [^DataStore store query]
  (log/info "document-search2" query)
  (let [url (.url store ["_search"])]
    (let [json-response (http/json-get url query)
          documents     (map source-with-id (-> json-response :hits :hits))]
      (log/info "search2: " query)
      {:count (count documents)
       :items documents})))

(defn search
  [^DataStore store search-term size page page-fn]
  (log/info "document-search" search-term size page)
  (let [base-url   (.url store ["_search"])
        search-url (str base-url "?" "q=" search-term "&" (pagination-param :size size :page page))
        all-url    (str base-url "?" (pagination-param :size size :page page))
        url        (if (clojure.string/blank? search-term) all-url search-url)]
    (log/info "Reading document count for " url)
    (let [json-response (http/json-get url)
          result-count  (-> json-response :hits :total)
          documents     (map source-with-id (-> json-response :hits :hits))]
      {:count (count documents)
       :items documents
       :links (page-links page-fn :size size :count result-count)})))

(defn es-error
  [e]
  {:error {:type           (-> e :error :type)
           :description    (str (-> e :error :reason) " line " (-> e :error :line) " column " (-> e :error :col))
           :query_location {:line (-> e :error :line)
                            :col  (-> e :error :col)}}})

(defn hash-child
  "Given a hash with a single key returns the value of that key"
  [h]
  (second (first h)))

(defn es-type-to-ktype
  [k value]
  (cond
    (= (-> value :type) "date") {k :timestamp}
    (= (-> value :type) "long") {k :long}
    (= (-> value :type) "keyword") {k :string}
    (= (-> value :type) "object") {k :object}
    (-> value :properties) {k :object}
    :else nil))

(defn es-mapping-to-ktypes
  "Takes an elastic search hashmap of field keys and types. Applies
  type conversion and returns a hashmap with the field names and kuona
  types"
  [h]
  (into {} (map (fn [k] (es-type-to-ktype (first k) (second k))) h)))


(defn index-settings
  [index-key]
  (:settings (get (http/json-get (stores/es-url index-key)) index-key)))

(defn indices
  []
  {:indices (into []
                  (map (fn [[k v]] (merge v {:name k :settings (index-settings k)}))
                       (:indices (http/json-get (stores/es-url "_stats")))))})

(defn read-schema
  [source]
  (log/info "read-schema" (:id source))
  (try+
    (let [id               (-> source :id)
          ^DataStore index (-> source :index)
          url              (.mapping-url index)
          body             (http/json-get url)
          mappings         (-> body hash-child hash-child hash-child hash-child)]
      {id (es-mapping-to-ktypes mappings)})
    (catch [:status 400] {:keys [body]}
      (let [error (parse-json body)]
        (log/info "Bad request" error)
        {:error {:type        (-> error :error :type)
                 :description (str (-> error :error :reason) " : " (-> error :error :resource.id))}}))
    (catch [:status 404] {:keys [body]}
      (let [error (parse-json body)]
        (log/info "Bad request" error)
        {:error {:type        (-> error :error :type)
                 :description (-> error :error :reason)}}))
    (catch Object _
      (log/error (:throwable &throw-context) "Unexpected error reading schema" source)
      {:error {:type        :unexpected
               :description (str (:throwable &throw-context))}})))
(defn query
  [^DataStore source q]
  (try+
    (let [store   (-> source :index)
          url     (.url store '("_search"))
          body    (http/json-get url q)
          results (map source-with-id (-> body :hits :hits))
          schema  (read-schema source)]
      {:count        (-> body :hits :total)
       :results      results
       :aggregations (merge {} (-> body :aggregations))
       :schema       schema})
    (catch [:status 400] {:keys [body]}
      (let [error (parse-json body)]
        (log/info "Bad request" error)
        (es-error error)))
    (catch Object _ {:error {:type :unexpected :description "Unknown error"}})))


(defn find-documents
  [url]
  (log/info "find-documents" url)
  (let [json-response (http/json-get url)
        documents     (map source-with-id (-> json-response :hits :hits))]
    {:count (count documents)
     :items documents
     :links []}))

(defn get-document
  [^DataStore mapping id]
  (try+
    (log/debug "getting" mapping id)
    (http/json-get (.url mapping [id]))
    (catch [:status 404] {:keys [body]}
      (let [error (parse-json body)]
        (es-error error))
      )))

(defn document-exists?
  [^DataStore store id]
  (try+
    (http/head (.url store [id]))
    true
    (catch [:status 404] {:keys []} false)))

(defn document-missing?
  [^DataStore store id]
  (not (document-exists? store id)))

(defn has-document?
  [^DataStore store id]
  (:found (get-document store id)))

(defn all-documents
  "Retrieves all the activity based on the supplied key from the index."
  [^DataStore store]
  (let [url      (.url store ["_search"] ["size=10000"])
        response (http/json-get url)]
    (map source-with-id
         (-> response
             :hits
             :hits))))

(defn delete-document [^DataStore store id]
  (let [url (.url store [id])]
    (log/info "delete-document " store id url)
    (http/delete url)))
