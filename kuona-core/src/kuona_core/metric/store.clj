(ns kuona-core.metric.store
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [kuona-core.util :refer :all]
            [kuona-core.elasticsearch :as es]
            [slingshot.slingshot :refer :all])
  (:gen-class))


(def collector-mapping-type
  {:properties {:name    es/string-not-analyzed
                :version es/string-not-analyzed}})

(def build-metric-mapping-type
  {:builds {:properties {:id        es/string-not-analyzed
                         :source    es/string-not-analyzed
                         :timestamp es/timestamp
                         :name      es/string-not-analyzed
                         :system    es/string-not-analyzed
                         :url       es/string-not-analyzed
                         :number    es/long-integer
                         :duration  es/long-integer
                         :result    es/string-not-analyzed
                         :collected es/timestamp
                         :collector collector-mapping-type
                         :jenkins   es/disabled-object}}})

(def repository-metric-type
  {:repositories {:properties {:source            es/string-not-analyzed
                               :name              es/string
                               :git_url           es/string-not-analyzed
                               :description       es/string
                               :avatar_url        es/string-not-analyzed
                               :project_url       es/string-not-analyzed
                               :created_at        es/timestamp
                               :updated_at        es/timestamp
                               :open_issues_count es/long-integer
                               :watchers          es/long-integer
                               :forks             es/long-integer
                               :size              es/long-integer
                               :last_analysed     es/timestamp
                               :github            es/enabled-object}}})


(def metric-mapping-type
  {:build {:properties {:timestamp es/timestamp,
                        :collector collector-mapping-type
                        :metric    {:properties {:activity  {:properties {:duration {:type "long"},
                                                                          :name     es/string-not-analyzed
                                                                          :id       es/string-not-analyzed
                                                                          :number   {:type "long"},
                                                                          :result   es/string-not-analyzed
                                                                          :type     es/string-not-analyzed}},
                                                 :collected es/timestamp,
                                                 :source    {:properties {:system es/string-not-analyzed,
                                                                          :url    es/string-not-analyzed}},
                                                 :type      es/string-not-analyzed}}}}
   :vcs   {:properties {:collector collector-mapping-type
                        :metric    {:properties {:activity  {:properties {:author        es/string
                                                                          :branches      es/string-not-analyzed
                                                                          :change_count  {:type "long"},
                                                                          :changed_files {:properties {:change es/string-not-analyzed
                                                                                                       :path   es/string-not-analyzed}}
                                                                          :email         es/string-not-analyzed
                                                                          :id            es/string-not-analyzed
                                                                          :merge         {:type "boolean"}
                                                                          :message       es/string}}
                                                 :collected es/timestamp
                                                 :name      es/string-not-analyzed
                                                 :source    {:properties {:system es/string-not-analyzed
                                                                          :url    es/string-not-analyzed}}
                                                 :type      {:type "keyword"}}}
                        :timestamp es/timestamp}}})


(defn health
  []
  (let [response (http/get "http://localhost:9200/_cluster/health")]
    (parse-json-body response)))


(defn http-path
  [& elements]
  (clojure.string/join "/" elements))


(defn index
  [index-name host]
  (clojure.string/join "/" [host (name index-name)]))

(defn has-index?
  "Test to see if the given elasticsearch index exists. Returns true if
  the index exists, false if the index does not exist and throws an
  exception if there is an error or unexpected response"
  [index]
  (log/debug "Testing index" index)
  (try+
    (let [response (http/head index)]
      (= (-> response :status) 200))
    (catch [:status 404] {:keys [request-time headers body]} false)
    (catch Object _
      (log/error (:throwable &throw-context) "unexpected error")
      (throw+))))


(def json-headers {"content-type" "application/json; charset=UTF-8"})

(defn create-index
  "Creates the currently configured index if it does not already
  exist."
  [index types]
  (try+
    (log/info "Creating index" index)
    (parse-json-body (http/put index {:headers json-headers
                                      :body    (generate-string {:mappings types})}))
    (catch [:status 400] {:keys [request-time headers body]}
      (log/error (str "Unexpected error creating index " index) body)
      (log/error (parse-json body))
      (log/error types)
      (throw+))
    (catch Object _
      (log/error (:throwable &throw-context) (str "Unexpected error creating index " index))
      (throw+)
      )))

(defn delete-index
  [index]
  (try+
    (parse-json-body (http/delete index))
    (catch Object _
      false)))

(defn delete-index-by-id
  [id]
  (try+
    (parse-json-body (http/delete (clojure.string/join "/" ["http://localhost" id] )))
    (catch Object _
      false)))

(defn mapping
  [mapping-name index]
  (clojure.string/join "/" [index (name mapping-name)]))

(defn put-document
  ([metric mapping] (put-document metric mapping (uuid)))
  ([metric mapping id]
   (let [url (clojure.string/join "/" [mapping id])]
     (log/info "put-document " mapping id url)
     (parse-json-body (http/put url {:headers json-headers
                                     :body    (generate-string metric)})))))


(defn put-partial-document
  [mapping id update]
  (let [url (clojure.string/join "/" [mapping id "_update"])
        request {:doc update}]
    (log/info "put-partial-update " mapping id)
    (parse-json-body (http/post url {:headers json-headers
                                     :body    (generate-string request)}))))

(defn get-count
  [mapping]
  (let [url (clojure.string/join "/" [mapping "_count"])]
    (log/info "Reading document count for " url)
    (parse-json-body (http/get url {:headers json-headers}))))

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
    (= (type n) java.lang.Long) n
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
  [mapping query]
  (log/info "internal-search" mapping)
  (let [url (clojure.string/join "/" [mapping "_search"])
        response (http/post url {:headers json-headers :body (generate-string query)})]
    (log/info "internal-search result" response)
    (parse-json-body response)))

(defn search
  [mapping search-term size page page-fn]
  (log/info "document-search" search-term size page)
  (let [base-url (clojure.string/join "/" [mapping "_search"])
        search-url (str base-url "?" "q=" search-term "&" (pagination-param :size size :page page))
        all-url (str base-url "?" (pagination-param :size size :page page))
        url (if (clojure.string/blank? search-term) all-url search-url)]
    (log/info "Reading document count for " url)
    (let [json-response (parse-json-body (http/get url {:headers json-headers}))
          result-count (-> json-response :hits :total)
          documents (map #(merge {:id (:_id %)} (:_source %)) (-> json-response :hits :hits))
          ]
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



(defn indices
  []
  {:indices (into []
                  (map (fn [[k v]] (merge v {:name k}))
                       (:indices (parse-json-body (http/get "http://localhost:9200/_stats")))))})

(defn read-schema
  [source]
  (log/info "read-schema" source)
  (try+
    (let [id (-> source :id)
          index (-> source :index)
          url (clojure.string/join "/" [index "_mapping"])
          response (http/get url {:headers json-headers})
          body (parse-json-body response)
          mappings (-> body hash-child hash-child hash-child hash-child)]
      {id (es-mapping-to-ktypes mappings)})
    (catch [:status 400] {:keys [request-time headers body]}
      (let [error (parse-json body)]
        (log/info "Bad request" error)
        {:error {:type        (-> error :error :type)
                 :description (str (-> error :error :reason) " : " (-> error :error :resource.id))}}))
    (catch [:status 404] {:keys [request-time headers body]}
      (let [error (parse-json body)]
        (log/info "Bad request" error)
        {:error {:type        (-> error :error :type)
                 :description (-> error :error :reason)}}))
    (catch Object _
      (log/error (:throwable &throw-context) "Unexpected error reading schema" source)
      {:error {:type        :unexpected
               :description (str (:throwable &throw-context))}})))


(defn query
  [source q]
  (try+
    (let [id (-> source :id)
          index (-> source :index)
          url (clojure.string/join "/" [index "_search"])
          response (http/get url {:headers json-headers :body (generate-string q)})
          body (parse-json-body response)
          results (map :_source (-> body :hits :hits))
          schema (read-schema source)]
      {:count   (-> body :hits :total)
       :results results
       :schema  schema})
    (catch [:status 400] {:keys [request-time headers body]}
      (let [error (parse-json body)]
        (log/info "Bad request" error)
        (es-error error)))
    (catch Object _ {:error {:type :unexpected :description "Unknown error"}})))


(defn find-documents
  [url]
  (log/info "store/find" url)
  (let [json-response (parse-json-body (http/get url {:headers json-headers}))
        result-count (-> json-response :hits :total)
        documents (map #(merge {:id (:_id %)} (:_source %)) (-> json-response :hits :hits))]
    {:count (count documents)
     :items documents
     :links []}))

(defn get-document
  [mapping id]
  (log/debug "getting" mapping id)
  (parse-json-body (http/get (clojure.string/join "/" [mapping id]))))

(defn has-document?
  [mapping id]
  (:found (get-document mapping id)))

(defn all-documents
  "Retrieves all the activity based on the supplied key from the index."
  [mapping]
  (log/debug "all documents in " mapping)
  (let [url (str (http-path mapping "_search") "?size=10000")
        query-string (generate-string {:query {:from 0 :size 10000}})
        query {:form-params query-string}
        response (http/get url)]
    (log/debug "all-documents response " response)
    (map #(:_source %)
         (-> response
             parse-json-body
             :hits
             :hits))))
