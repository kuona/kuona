(ns kuona-core.metric.store
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [kuona-core.util :refer :all]
            [slingshot.slingshot :refer :all])
  (:gen-class))

(def es-string-type
  {:type "text"})

(def es-string-not-analyzed-type
  {:type "keyword" :index "not_analyzed"})

(def es-timestamp-type
  {:type "date" :format "strict_date_optional_time||epoch_millis"})

(def collector-mapping-type
  {:properties {:name    es-string-not-analyzed-type
                :version es-string-not-analyzed-type}})

(def metric-mapping-type
  {:build {:properties {:timestamp es-timestamp-type,
                        :collector collector-mapping-type
                        :metric    {:properties {:activity  {:properties {:duration {:type "long"},
                                                                          :name       es-string-not-analyzed-type
                                                                          :id       es-string-not-analyzed-type
                                                                          :number   {:type "long"},
                                                                          :result   es-string-not-analyzed-type
                                                                          :type     es-string-not-analyzed-type}},
                                                 :collected es-timestamp-type,
                                                 :source    {:properties {:system es-string-not-analyzed-type,
                                                                          :url    es-string-not-analyzed-type}},
                                                 :type      es-string-not-analyzed-type}}}}
   :vcs {:properties {:collector collector-mapping-type
                      :metric {:properties {:activity {:properties {:author        es-string-type
                                                                    :branches      es-string-not-analyzed-type
                                                                    :change_count  {:type "long"},
                                                                    :changed_files {:properties {:change es-string-not-analyzed-type
                                                                                                 :path es-string-not-analyzed-type}}
                                                                    :email         es-string-not-analyzed-type
                                                                    :id            es-string-not-analyzed-type
                                                                    :merge         {:type "boolean"}
                                                                    :message       es-string-type}}
                                            :collected es-timestamp-type
                                            :name es-string-not-analyzed-type
                                            :source {:properties {:system es-string-not-analyzed-type
                                                                  :url es-string-not-analyzed-type}}
                                            :type {:type "keyword"}}}
                      :timestamp es-timestamp-type}}})

(defn http-path
  [& elements]
  (clojure.string/join "/" elements))


(defn index
  [index-name host]
  (clojure.string/join "/" [host (name index-name)]))

(defn has-index?
  "Returns the index or false if the index does not exist"
  [index]
  (log/debug "Testing index" index)
  (try+
   (parse-json-body (http/get index))
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
                                     :body (generate-string {:mappings types})}))
   (catch Object _
       (log/error (:throwable &throw-context) "unexpected error")
       (throw+)
       )))

(defn delete-index
  [index]
  (try+
   (parse-json-body (http/delete index))
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
                                     :body (generate-string metric)})))))


(defn put-partial-document
  [mapping id update]
  (let [url (clojure.string/join "/" [mapping id "_update"])
        request {:doc update}]
    (log/info "put-partial-update " mapping id)
    (parse-json-body (http/post url {:headers json-headers
                                     :body (generate-string request)}))))

(defn get-count
  [mapping]
  (let [url (clojure.string/join "/" [mapping "_count"]) ]
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
    :else (try+ (. Integer parseInt  n)
                (catch Object _ nil))))
                                        

(defn pagination-param
  [&{:keys [:size :page]}]
  (let [page-number (parse-integer page)]
    (cond
      (nil? page-number) (str "size=" size)
      (= page-number 1)  (str "size=" size)
      :else              (str "size=" size "&" "from=" (* (- page-number 1) size)))))

(defn internal-search
  [mapping query]
  (log/info "internal-search" mapping)
  (let [url (clojure.string/join "/" [mapping "_search"])
        response (http/post url {:headers json-headers :body (generate-string query)})]
    (log/info "internal-search result" response)
    (parse-json-body response)))

(defn search
  ([mapping search-term] (search mapping search-term 100))
  ([mapping search-term size page page-fn]
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
        :links (page-links page-fn :size size :count result-count)}))))

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
  (log/debug  "all documents in " mapping)
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
