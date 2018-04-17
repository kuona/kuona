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

(defn mapping-url [index]
  (string/join "/" [(-> index :url) "_mapping"]))

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
  (log/debug "Testing index" index)
  (try+
    (let [response (http/head index)]
      (= (-> response :status) 200))
    (catch [:status 404] {:keys [request-time headers body]} false)
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


(def builds-store (mapping :builds (index :kuona-builds default-es-host)))
(def collector-activity-store (mapping :activity (index :kuona-collectors default-es-host)))
(def collector-config-store (mapping :collector (index :kuona-collector-config default-es-host)))
(def environments-store (mapping :environments (index :kuona-env default-es-host)))
(def environments-comment-store (mapping :comments (index :kuona-env default-es-host)))
(def metrics-store (index :kuona-metrics default-es-host))
(def snapshots-store (mapping :snapshots (index :kuona-snapshots default-es-host)))
(def repositories-store (mapping :repositories (index :kuona-repositories default-es-host)))
(def commit-logs-store (mapping :commit-log (index :kuona-vcs-commit default-es-host)))
(def code-metric-store (mapping :content (index :kuona-vcs-content default-es-host)))

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



(defn create-store-if-missing
  [store schema]
  (if (has-index? store) nil (create-index store schema)))

(defn create-stores []
  (create-store-if-missing repositories-store repository-metric-type)
  (create-store-if-missing snapshots-store {:snapshots {}})
  (create-store-if-missing builds-store build-metric-mapping-type)
  (create-store-if-missing collector-activity-store collector-activity-schema)
  (create-store-if-missing collector-config-store collector-config-schema))

(defn count-url [index]
  (string/join "/" [(-> index :url) "_count"]))

(defn option-to-es-search-param [[k v]]
  (cond
    (= k :term) (str "q=" v)
    (= k :size) (str "size=" v)
    (= k :from) (str "from=" v)
    :else nil
    ))

(defn es-options [options]
  (select-keys options '(:term :size :from)))

(defn parse-integer
  [n]
  (cond
    (= (type n) java.lang.Long) n
    :else (try+ (. Integer parseInt n)
                (catch Object _ nil))))
(defn pagination-param
  [options]
  (let [page (-> options :page)
        size (-> options :size)
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

(defn search-url
  ([index]
   (string/join "/" [(-> index :url) "_search"]))

  ([index options]
   (let [query-string (query-string options)
         path (string/join "/" [(-> index :url) "_search"])]
     (str path (if options (str "?" query-string))))))

(defn id-url [index id]
  (string/join "/" [(-> index :url) id]))

(defn update-url [index id]
  (string/join "/" [(-> index :url) id "_update"]))

