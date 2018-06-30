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
(def default-store-prefix "kuona")

(def es-host (atom default-es-host))

(def store-prefix (atom default-store-prefix))

(defn set-store-prefix [prefix]
  (reset! store-prefix prefix))

(defn es-index
  [index-name type]
  {:name (name index-name)
   :url  (string/join "/" [(deref es-host) (name index-name)])
   :type type})

(defn es-url
  [& elements]
  (string/join "/" (map name (concat [(deref es-host)] elements))))

(defn- index
  [index-name host]
  (clojure.string/join "/" [host (name index-name)]))

(defn- mapping
  [mapping-name index]
  (clojure.string/join "/" [index (name mapping-name)]))

(defn store-exists?
  "Test to see if the given elasticsearch index exists. Returns true if
  the index exists
 false if the index does not exist and throws an
  exception if there is an error or unexpected response"
  [index]
  (log/info "Testing store exists" index)
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
    (log/info "Deleting store " index)
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
  {:activity {:properties {:activity  {:type   "text"

                                       :fields {:keyword {:type         "keyword"
                                                          :ignore_above 256}}}

                           :collector {:properties {:name    es/indexed-keyword

                                                    :version es/indexed-keyword}}

                           :id        es/indexed-keyword

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
  {:build {:properties {:timestamp es/timestamp

                        :collector collector-mapping-type
                        :metric    {:properties {:activity  {:properties {:duration {:type "long"}

                                                                          :name     es/string-not-analyzed
                                                                          :id       es/string-not-analyzed
                                                                          :number   {:type "long"}

                                                                          :result   es/string-not-analyzed
                                                                          :type     es/string-not-analyzed}}

                                                 :collected es/timestamp

                                                 :source    {:properties {:system es/string-not-analyzed

                                                                          :url    es/string-not-analyzed}}

                                                 :type      es/string-not-analyzed}}}}
   :vcs   {:properties {:collector collector-mapping-type
                        :metric    {:properties {:activity  {:properties {:author        es/string
                                                                          :branches      es/string-not-analyzed
                                                                          :change_count  {:type "long"}

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


(def snapshot-schema
  {:snapshots {:properties {:build      {:properties {:artifact     {:properties {:artifactId es/indexed-keyword
                                                                                  :groupId    es/indexed-keyword
                                                                                  :name       es/indexed-keyword
                                                                                  :packaging  es/indexed-keyword
                                                                                  :version    es/indexed-keyword}}
                                                      :builder      es/indexed-keyword
                                                      :dependencies {:properties {:dependencies {:properties {:from {:properties {:artifactId es/indexed-keyword
                                                                                                                                  :groupId    es/indexed-keyword
                                                                                                                                  :id         es/indexed-keyword
                                                                                                                                  :packaging  es/indexed-keyword
                                                                                                                                  :scope      es/indexed-keyword
                                                                                                                                  :version    es/indexed-keyword}}
                                                                                                              :to   {:properties {:artifactId es/indexed-keyword
                                                                                                                                  :groupId    es/indexed-keyword
                                                                                                                                  :id         es/indexed-keyword
                                                                                                                                  :packaging  es/indexed-keyword
                                                                                                                                  :scope      es/indexed-keyword
                                                                                                                                  :version    es/indexed-keyword}}}}
                                                                                  :root         {:properties {:artifactId es/indexed-keyword
                                                                                                              :groupId    es/indexed-keyword
                                                                                                              :id         es/indexed-keyword
                                                                                                              :packaging  es/indexed-keyword
                                                                                                              :version    es/indexed-keyword}}}}
                                                      :path         es/indexed-keyword}}
                            :content    {:properties {:blank_line_details   {:properties {:language es/indexed-keyword}}
                                                      :code_line_details    {:properties {:language es/indexed-keyword}}
                                                      :comment_line_details {:properties {:language es/indexed-keyword}}
                                                      :file_details         {:properties {:language es/indexed-keyword}}}}
                            :repository {:properties {:description       es/indexed-keyword
                                                      :open_issues_count {:type "long"}
                                                      :forks_count       {:type "long"}
                                                      :watchers_count    {:type "long"}
                                                      :name              es/indexed-keyword
                                                      :stargazers_count  {:type "long"}
                                                      :owner_avatar_url  es/indexed-keyword
                                                      :size              {:type "long"}
                                                      :updated_at        {:type "date"}
                                                      :language          es/indexed-keyword
                                                      :pushed_at         {:type "date"}}}}}}
  )

(def commit-log-schema
  {:commit-log {:properties {:timestamp     es/timestamp
                             :repository_id es/indexed-keyword
                             :commit        {:properties {:author        es/indexed-keyword,
                                                          :branches      es/indexed-keyword,
                                                          :change_count  es/long-integer
                                                          :changed_files {:properties {:change es/indexed-keyword
                                                                                       :path   es/indexed-keyword}}
                                                          :email         es/indexed-keyword,
                                                          :id            es/indexed-keyword,
                                                          :merge         es/boolean-type
                                                          :message       es/string
                                                          :time          es/timestamp}}
                             :source        {:properties {:system es/indexed-keyword,
                                                          :url    es/indexed-keyword}}
                             :collected     es/timestamp
                             :collector     {:properties {:name    es/indexed-keyword
                                                          :version es/indexed-keyword}}
                             }
                }

   })

(def code-metric-schema
  )

(defn create-store-if-missing
  [store schema]
  (cond
    (store-exists? store) (log/info "Store" store "exists")
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
  (destroy [this] "Destroys the index and all content in that index")
  (mapping-url [this] "URL for reading the index schema")
  (url [this] [this args] [this path params] "returns the URL for the store"))


(defn- index-name [iname]
  (str (deref store-prefix) "-" (name iname)))

(defn- data-store-index-name [store]
  (index (index-name (-> store :index-name)) default-es-host))

(defrecord DataStore [index-name mapping-name schema]
  Store
  (exists? [this] (store-exists? (data-store-index-name this)))
  (create [this] (create-store-if-missing (data-store-index-name this) (-> this :schema)))
  (destroy [this] (delete-index (data-store-index-name this)))
  (mapping-url [this] (string/join "/" [(data-store-index-name this) "_mapping"]))
  (url [this] (mapping (-> this :mapping-name) (data-store-index-name this)))
  (url [this args]
    (let [m        (mapping (-> this :mapping-name) (data-store-index-name this))
          elements (into [m] args)]
      (string/join "/" elements)))
  (url [this args params]
    (let [m        (mapping (-> this :mapping-name) (data-store-index-name this))
          elements (into [m] args)
          p        (string/join "&" params)]
      (str (string/join "/" elements) "?" p))))

(def environments-store (mapping :environments (index :kuona-env default-es-host)))
(def environments-comment-store (mapping :comments (index :kuona-env default-es-host)))
(def metrics-store (index :kuona-metrics default-es-host))

(def repositories-store (DataStore. :repositories :repositories repository-metric-type))
(def snapshots-store (DataStore. :snapshots :snapshots snapshot-schema))
(def builds-store (DataStore. :builds :builds build-metric-mapping-type))
(def collector-activity-store (DataStore. :collectors :activity collector-activity-schema))
(def collector-config-store (DataStore. :collector-config :collector collector-config-schema))
(def commit-logs-store (DataStore. :vcs-commit :commit-log commit-log-schema))
(def code-metric-store (DataStore. :vcs-content :content {}))
(def dashboards-store (DataStore. :dashboards :dashboard dashboards-schema))

(def sources
  {:builds             {:id          :builds
                        :index       builds-store
                        :description "Build data - software construction data read from Jenkins"
                        :path        "/api/query/builds"}
   :snapshots          {:id          :snapshots
                        :index       snapshots-store
                        :description "Snapshot data from source code analysis"}
   :repositories       {:id          :repositories
                        :index       repositories-store
                        :description "Captured repository data"}
   :commits            {:id          :commits
                        :index       commit-logs-store
                        :description "Captured commit data"}
   :code               {:id          :code
                        :index       code-metric-store
                        :description "Results of source analysis"}
   :collector-activity {:id          :collector-activity
                        :index       collector-activity-store
                        :description "Records collector activity"}
   :collector-config   {:id          :collector-config
                        :index       collector-config-store
                        :description "Stores the configuration of configured collectors"}
   :dashboards         {:id          :dashboards
                        :index       dashboards-store
                        :description "Dashboard configuration store"}})

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

(defn rebuild-source
  [source]
  (let [^DataStore index (-> source :index)]
    (log/info "Rebuilding store index " index)
    (.destroy index)
    (.create index)))

(defn rebuild []
  (doall (map rebuild-source (vals sources))))

(defn find-store-by-name [name]
  (let [source (first (filter (fn [source] (= name
                                              (index-name (-> source :index :index-name)))) (vals sources)))]
    (-> source :index)))

(defn rebuild-store-by-name
  [name]
  (let [store (find-store-by-name name)]
    (cond
      (nil? store) (do
                     (log/warn "Requested index rebuld for " name "nor found")
                     {:error ("Requested index '" name "' not found")})
      :else (do
              (log/info "Rebuilding  " name)
              (.destroy store)
              (.create store)
              {:status      :ok
               :description (str "Store " name " rebuilt (empty)")}))))
