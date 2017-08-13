(ns kuona-core.git
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as git-query]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [slingshot.slingshot :refer :all]
            [kuona-core.util :refer :all]
            [kuona-core.cloc :as cloc]
            [kuona-core.metric.store :as store])
  (:gen-class))

(defn local-clone-path
  [dir repo]
  (canonical-path (clojure.string/join "/" [dir (uuid-from repo)])))

(defn file-change
  [path change]
  {:path path :change change})

(defn git-clone
  [url path]
  (log/debug "Cloning " url " to " path)
  (git/git-clone-full url path))

(defn git-pull
  [url path]
  (let [repo (git/load-repo path)]
    (log/debug "Updating local repository for" url)
    (git/git-checkout repo "master")
    (git/git-pull repo)))

(defn changes-to-map
  "Takes list of two element arrays and returns an array of maps"
  [changes]
  (into [] (map #(file-change (first %) (second %))  changes)))

(defn commit-and-diff
  [mapping url repo commit]
  (let [commit-info (git-query/commit-info repo commit)
        id          (:id commit-info)
        metric      {:timestamp (:time commit-info)
                     :metric    {:type      :commit
                                 :name      "TBD"
                                 :source    {:system :git
                                             :url    url}
                                 :activity  {:type          :commit
                                             :email         (:email commit-info)
                                             :branches      (:branches commit-info)
                                             :change_count  (count (changes-to-map (:changed_files commit-info)))
                                             :changed_files (changes-to-map (:changed_files commit-info))
                                             :merge         (:merge commit-info)
                                             :author        (:author commit-info)
                                             :message       (:message commit-info)
                                             :id            (:id commit-info)}
                                 :collected (timestamp)}
                     :collector {:name    :kuona-git-collector
                                 :version "0.1"}}]
    (let [result (store/put-document metric mapping id)]
      (log/info "Processing commit " url " " id " @ " (:time commit-info))
      result)))

(defn updated-metric?
  [metric]
  (= (-> metric :result) "updated"))

(defn log-metrics
  [mapping url path]
  (let [repo (git/load-repo path)]
    (log/info "Collecting commit metrics for " url " master branch ")
    (git/git-checkout repo "master")
    (first (filter #(updated-metric? %) (map #(commit-and-diff mapping url repo %) (git/git-log repo))))))

(defn each-commit
  "Apply the function f to each version of the repository - based on the log"
  [f repo-path]
  (let [repo (git/load-repo repo-path)]
    (doseq [log (git/git-log repo)]
      (do (git/git-checkout repo (.getName log))
          (f repo-path (.getName log) (:time (git-query/commit-info repo log)))
          (git/git-checkout repo "master")))))

(defn commit-history
  ([repo]
   (let [log  (git/git-log repo)
         keys [:email :time :branches :changed_files :merge false :author :id :message]]
     (map (fn[c] (select-keys (git-query/commit-info repo c) keys)) log)))
  ([repo start]
   (let [commits (commit-history repo)]
     (drop-while (fn [c] (not= (:id c) start)) commits)))
  ([repo start end]
   (let [commits (commit-history repo start)
         group   (take-while (fn [c] (not= (:id c) end)) commits)]
     (concat group [(first (drop (count group) commits))]))))

(defn commits
  [repo-path]
  (log/info "commits " repo-path)
  (let [repo   (git/load-repo repo-path)
        master (git/git-checkout repo "master")
        head   (git/git-checkout repo "HEAD")]
    (git/git-log repo)))

(defn commit-by-day
  [repo-path]
  (let [repo    (git/load-repo repo-path)
        master  (git/git-checkout repo "master")
        head    (git/git-checkout repo "HEAD")
        history (git/git-log repo)]
    (group-by #(t/with-time-at-start-of-day (tc/from-date (:time (git-query/commit-info repo %)))) history)))

(defn each-commit-by-day
  "Apply the function f to each version of the repository - based on the log"
  [f repo-path]
  (let [repo            (git/load-repo repo-path)
        history         (git/git-log repo)
        grouped-commits (group-by #(t/with-time-at-start-of-day (tc/from-date (:time (git-query/commit-info repo %)))) history)]
    (log/info "Found " (count history) " commits on " (count grouped-commits) " distinct days")
    (doseq [log grouped-commits]
      (do (let [c (-> log second first)]
            (git/git-checkout repo (.getName c))
            (f repo-path (.getName c) (:time (git-query/commit-info repo c))))
          (git/git-checkout repo "master")))))

(defn collect
  [vcs-mapping code-mapping workspace url]
  (try+
   (let [local-dir (local-clone-path workspace url)]
     (log/info "Collecting " url " to " local-dir)
     (if (directory? local-dir) (git-pull url local-dir) (git-clone url local-dir))
     (log-metrics vcs-mapping url local-dir)
     (each-commit-by-day
      (fn [path sha timestamp]
          (cloc/loc-collector
           (fn [a]
             (log/info url " @ " timestamp)
             (store/put-document {:timestamp timestamp
                                  :metric    {:source    {:system :git :url url}
                                              :type      :loc
                                              :name      "TBD"
                                              :collected (-> a :metric :collected)
                                              :activity  (-> a :metric :activity)}
                                  :collector (-> a :collector)}
                                 code-mapping (uuid-from sha "cloc"))) path sha))
        local-dir)
     ) 
   (catch Object _
     (log/error (:throwable &throw-context) "unexpected error collecting metrics for " url workspace))))
