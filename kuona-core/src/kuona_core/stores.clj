(ns kuona-core.stores
  (:require [clojure.string :as string]
            [slingshot.slingshot :refer :all]
            [cheshire.core :refer [generate-string]]
            [clojure.tools.logging :as log]
            [clj-http.client :as http]
            [kuona-core.elasticsearch :as es]
            [kuona-core.util :as util])
  (:gen-class))


(def default-es-host "http://localhost:9200")

(def es-host (atom default-es-host))

(defn es-index
  [index-name type]
  {:name (name index-name)
   :url  (string/join "/" [(deref es-host) (name index-name)])
   :type type})

(defn- index
  [index-name host]
  (clojure.string/join "/" [host (name index-name)]))

(defn- mapping
  [mapping-name index]
  (clojure.string/join "/" [index (name mapping-name)]))

(defn has-index?
  "Test to see if the given elasticsearch index exists. Returns true if
  the index exists, false if the index does not exist and throws an
  exception if there is an error or unexpected response"
  [index]
  (log/info "Testing index" index)
  (try+
    (let [response (http/head index)]
      (= (-> response :status) 200))
    (catch [:status 404] {:keys [request-time headers body]} false)
    (catch [:status 405] {:keys [request-time headers body]} false)
    (catch Object _
      (log/error (:throwable &throw-context) "unexpected error")
      (throw+))))

(defn create-index
  "Creates the currently configured index if it does not already
  exist."
  [index types]
  (try+
    (log/info "Creating index" index)
    (util/parse-json-body (http/put index {:headers util/json-headers
                                           :body    (generate-string {:mappings types})}))
    (catch [:status 400] {:keys [request-time headers body]}
      (log/error (str "Unexpected error creating index " index) body)
      (log/error (util/parse-json body))
      (log/error types)
      (throw+))
    (catch Object _
      (log/error (:throwable &throw-context) (str "Unexpected error creating index " index))
      (throw+)
      )))

(defn delete-index
  [index]
  (try+
    (util/parse-json-body (http/delete index))
    (catch Object _
      false)))

(defn delete-index-by-id
  [id]
  (try+
    (util/parse-json-body (http/delete (clojure.string/join "/" [default-es-host id])))
    (catch Object _
      false)))




(def keyword-field
  {:type "text" :fields {:keyword {:type "keyword" :ignore_above 256}}})

(def collector-activity-schema
  {:activity {:properties {:activity  {:type   "text",
                                       :fields {:keyword {:type "keyword", :ignore_above 256}}},
                           :collector {:properties {:name    {:type "text", :fields {:keyword {:type "keyword", :ignore_above 256}}},
                                                    :version {:type "text", :fields {:keyword {:type "keyword", :ignore_above 256}}}}},
                           :id        {:type "text", :fields {:keyword {:type "keyword", :ignore_above 256}}},
                           :timestamp {:type "date"}}}})

(def collector-config-schema
  {:collector {:properties
               {:collector      es/string-analyzed
                :collector_type es/string-analyzed
                :config         {:properties {:url      es/string
                                              :username es/string
                                              :password es/string-not-analyzed}}}}})


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



;; Needs to be split into two stores or removed.
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

(def dashboards-schema {:dashboard {:properties {:id          es/string-not-analyzed
                                                 :name        es/string-not-analyzed
                                                 :description es/string}}})


(defn create-store-if-missing
  [store schema]
  (cond
    (has-index? store) (log/info "Store" store "exists")
    :else (create-index store schema)))


(defn count-url [index]
  (string/join "/" [(-> index :url) "_count"]))

(defn option-to-es-search-param [[k v]]
  (cond
    (= k :term) (str "q=" v)
    (= k :size) (str "size=" v)
    (= k :from) (str "from=" v)
    :else nil
    ))

(defn parse-integer
  [n]
  (cond
    (= (type n) java.lang.Long) n
    :else (try+ (. Integer parseInt n)
                (catch Object _ nil))))
(defn pagination-param
  [options]
  (let [page        (-> options :page)
        size        (-> options :size)
        page-number (parse-integer page)]
    (cond
      (and size (> page-number 1)) {:size size :from (* (- page-number 1) size)}
      (nil? size) {}
      (= page-number 1) {:size size}
      :else {})
    ))

(defn query-string [options]
  (let [pagination (pagination-param options)]
    (string/join "&" (filter #(not (nil? %)) (map option-to-es-search-param (merge options pagination))))))


(defprotocol Store
  (exists? [this] "Tests for the stores existence")
  (create [this] "Creates the store if it does not exist")
  (mapping-url [this] "URL for reading the index schema")
  (url [this] [this args] [this path params] "returns the URL for the store"))


(defrecord DataStore [index-name mapping-name schema]
  Store
  (exists? [this] (has-index? (index (-> this :index-name) default-es-host)))
  (create [this] (create-store-if-missing (index (-> this :index-name) default-es-host) (-> this :schema)))
  (mapping-url [this] (string/join "/" [(index (-> this :index-name) default-es-host) "_mapping"]))
  (url [this] (mapping (-> this :mapping-name) (index (-> this :index-name) default-es-host)))
  (url [this args]
    (let [m        (mapping (-> this :mapping-name) (index (-> this :index-name) default-es-host))
          elements (into [m] args)]
      (string/join "/" elements)))
  (url [this args params]
    (let [m        (mapping (-> this :mapping-name) (index (-> this :index-name) default-es-host))
          elements (into [m] args)
          p        (string/join "&" params)]
      (str (string/join "/" elements) "?" p)
      )))

(def environments-store (mapping :environments (index :kuona-env default-es-host)))
(def environments-comment-store (mapping :comments (index :kuona-env default-es-host)))
(def metrics-store (index :kuona-metrics default-es-host))

(def repositories-store (DataStore. :kuona-repositories :repositories repository-metric-type))
(def snapshots-store (DataStore. :kuona-snapshots :snapshots {}))
(def builds-store (DataStore. :kuona-builds :builds build-metric-mapping-type))
(def collector-activity-store (DataStore. :kuona-collectors :activity collector-activity-schema))
(def collector-config-store (DataStore. :kuona-collector-config :collector collector-config-schema))
(def commit-logs-store (DataStore. :kuona-vcs-commit :commit-log {}))
(def code-metric-store (DataStore. :kuona-vcs-content :content {}))
(def dashboards-store (DataStore. :kuona-dashboards :dashboard dashboards-schema))

(def sources
  {:builds       {:id          :builds
                  :index       builds-store
                  :description "Build data - software construction data read from Jenkins"
                  :path        "/api/query/builds"}
   :snapshots    {:id          :snapshots
                  :index       snapshots-store
                  :description "Snapshot data from source code analysis"}
   :repositories {:id          :repositories
                  :index       repositories-store
                  :description "Captured repository data"}
   :commits      {:id          :commits
                  :index       commit-logs-store
                  :description "Captured commit data"}
   :code         {:id          :code
                  :index       code-metric-store
                  :description "Results of source analysis"}})

(defn create-stores
  []
  (log/info "Creating missing data stores")
  (.create repositories-store)
  (.create snapshots-store)
  (.create builds-store)
  (.create collector-activity-store)
  (.create collector-config-store)
  (.create snapshots-store)
  (.create commit-logs-store)
  (.create code-metric-store)
  (.create dashboards-store))
