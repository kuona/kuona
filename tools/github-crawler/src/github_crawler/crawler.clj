(ns github-crawler.crawler
  (:require
    [clojure.tools.logging :as log]
    [clj-http.client :as http]
    [cheshire.core :refer :all]
    [slingshot.slingshot :refer :all]
    [kuona-core.git :refer :all]
    [kuona-core.util :as util])
  (:import (java.util Date))
  (:gen-class))

(def config-file "properties.edn")
(def progress-file "progress.edn")
(def page-file "page.edn")

(defn epoc-date
  [d]
  (Date. (* 1000 (Long/parseLong d))))

(defn write-config
  [filename config]
  (with-open [w (clojure.java.io/writer filename)]
    (.write w (prn-str config))))

(defn github-next-page
  [headers]
  (let [link (get headers "Link")
        m (re-matches #"<([^>]*page=(\d+))[^>]*>; rel=\"next.*" link)]
    (if (second m) (Long/parseLong (second (rest m))) nil)))

(defn wait-for-reset
  [time]
  (let [now (Date.)
        wait-time (- (+ (.getTime time) 60000) (.getTime now))]
    (if (> wait-time 0)
      (do
        (log/info "Waiting until " time "(" now ") for " wait-time "ms" (quot wait-time 60000) " minutes")
        (Thread/sleep 10000)
        (recur time)))))

(defn rate-limit
  [headers]
  (let [remaining (Integer/parseInt (get headers "X-RateLimit-Remaining"))
        rate-reset (epoc-date (get headers "X-RateLimit-Reset"))]
    (log/info "Remaining " remaining " Resets " rate-reset " current " (Date.))
    (if (= remaining 0) (wait-for-reset rate-reset) (Thread/sleep 1000))))


(defn limited-get
  [url]
  (try+
    (log/info "limited get" url)
    (http/get url)
    (catch [:status 403] {:keys [request-time headers body]}
      (log/info "403 rate limit block")
      (wait-for-reset (epoc-date (get headers "X-RateLimit-Reset")))
      (limited-get url))
    (catch Object _
      (log/error (:throwable &throw-context) "unexpected error")
      (throw+))))

(defn add-url-paramter
  [url key values]
  (if (key values)
    (str url "&" (key values))
    url))

(defn parameter-value
  [v]
  (cond
    (keyword? v) (name v)
    :else v))

(defn url-parameter
  [pair]
  (str (name (first pair)) "=" (parameter-value (second pair))))

(defn query-string
  [m]
  (clojure.string/join "&" (map url-parameter m)))


(defn github-search
  [language page ctx]
  (let [params (merge {:q     (str "language:" language)
                       :sort  "stars"
                       :order "asc"
                       :page  page
                       }
                      (select-keys ctx [:client_id :client_secret]))
        query-string (query-string params)
        query-uri (str "https://api.github.com/search/repositories?" query-string)
        response (limited-get query-uri)
        headers (-> response :headers)
        remaining (Integer/parseInt (get headers "X-RateLimit-Remaining"))
        rate-reset (epoc-date (get headers "X-RateLimit-Reset"))
        items (-> (parse-string (-> response :body) true) :items)]
    (log/info "Remaining " remaining " Resets " rate-reset " current " (Date.))
    (log/info "Collected" query-uri)
    {:next  (github-next-page headers)
     :items items}))

(defn github-since
  [headers]
  (let [link (get headers "Link")
        m (re-matches #"<([^>]*since=(\d+))[^>]*>; rel=\"next.*" link)]
    (println "Link header:" link headers)
    (if (second m) (Long/parseLong (second (rest m))) nil)))

(defn process-repositories-response
  [response]
  (let [headers (-> response :headers)
        items (parse-string (-> response :body) true)]
    (rate-limit (-> response :headers))
    {:next  (github-since headers)
     :items items}))

(defn github-repositories
  ([ctx]
   (let [url (str "https://api.github.com/repositories?" (query-string (select-keys ctx [:client_id :client_secret])))]
     (log/info "reading repositories " url)
     (process-repositories-response (limited-get url))))
  ([ctx since]
   (let [url (str "https://api.github.com/repositories?" (query-string (merge {:since since} (select-keys ctx [:client_id :client_secret]))))]
     (log/info "reading repositories " url)
     (process-repositories-response (limited-get url)))))

(defn git-url
  [item]
  (-> item :git_url))

(defn put-repository
  [metric id]
  (let [url (clojure.string/join "/" ["http://dashboard.kuona.io/api/repositories" id])]
    (log/info "put-repository " (-> metric :project :name) url)
    (util/parse-json-body (http/put url {:headers {"content-type" "application/json; charset=UTF-8"}
                                         :body    (generate-string metric)}))))

(defn process-item
  [context item]
  ;(log/info "processing " (:workspace context) (or (:git_url item) (:url item)))
  (cond
    (:git_url item)
    (let [url (:git_url item)
          working-space (local-clone-path (:workspace context) url)
          metric {:source          :github
                  :github_language nil
                  :url             url
                  :project         item
                  :last_analysed   nil}
          id (util/uuid-from url)]
      (put-repository metric id))
    :else (println "Skipping - no gitub url")))

(defn process-items
  [context items]
  (doseq [item items] (process-item context item)))

(defn collect-repositories
  ([context]
   (log/info "collecting from the start")
   (let [m (github-repositories context)]
     (process-items context (:items m))
     (collect-repositories context (:next m))))
  ([context since]
   (log/info "collecting from " since)
   (let [m (github-repositories context since)]
     (process-items context (:items m))
     (write-config "properties.edn" {:high-water-mark since})
     (collect-repositories context (:next m)))))

(defn search-collect
  ([ctx language]
   (let [page-file (-> ctx :page-file)
         pages (util/load-config page-file)
         page (if (-> ctx :force) 1 (get pages language 1))]
     (search-collect ctx language page)))
  ([ctx language page]
   (log/info "Collecting" language "page" page)
   (let [page-file (-> ctx :page-file)
         pages (util/load-config page-file)
         m (github-search language page ctx)]
     (write-config page-file (merge pages {language (:next m)}))
     (process-items ctx (:items m))
     (search-collect ctx language (:next m)))))

(defn crawl
  [config]
  (log/info "Gighub client id     " (-> config :client_id))
  (log/info "Using local Workspace" (-> config :workspace))
  (log/info "Updating             " (-> config :api-url))
  (log/info "For languages        " (-> config :languages))
  (log/info "Status tracking file " (-> config :page-file))

  (doseq [language (-> config :languages)] (search-collect config language)))

