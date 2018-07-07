(ns kuona-core.git
  (:require [clojure.tools.logging :as log]
            [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as git-query]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [slingshot.slingshot :refer :all]
            [kuona-core.util :refer :all]
            [kuona-core.cloc :as cloc]
            [kuona-core.store :as store])
  (:gen-class))

(defn local-clone-path
  [dir repo]
  (canonical-path (clojure.string/join "/" [dir (uuid-from repo)])))

(defn file-change
  [path change]
  {:path path :change change})

(defn git-clone
  [url path]
  (log/info "Cloning " url " to " path)
  (git/with-identity {:name "~/.ssh/id_rsa" :exclusive true}
                     (git/git-clone-full url path)))

(defn git-pull
  [url path]
  (let [repo (git/load-repo path)]
    (log/info "Updating local repository for" path url)
    (git/with-identity {:name "~/.ssh/id_rsa" :exclusive true}
                       (log/info "Checkout master")
                       (git/git-checkout repo "master")
                       (log/info "reset master")
                       (git/git-reset repo "master" :hard)
                       (log/info "pull updates")
                       (git/git-pull repo))))

(defn changes-to-map
  "Takes list of two element arrays and returns an array of maps"
  [changes]
  (into [] (map #(file-change (first %) (second %)) changes)))

(defn commit-and-diff
  [commit-store url repo commit repository-id]
  (let [commit-info (git-query/commit-info repo commit)
        id          (:id commit-info)
        metric      {:timestamp     (:time commit-info)
                     :repository_id repository-id
                     :commit        {:time          (:time commit-info)
                                     :email         (:email commit-info)
                                     :branches      (:branches commit-info)
                                     :change_count  (count (changes-to-map (:changed_files commit-info)))
                                     :changed_files (changes-to-map (:changed_files commit-info))
                                     :merge         (:merge commit-info)
                                     :author        (:author commit-info)
                                     :message       (:message commit-info)
                                     :id            (:id commit-info)}
                     :source        {:system :git
                                     :url    url}
                     :collected     (timestamp)
                     :collector     {:name    :kuona-git-collector
                                     :version "0.1"}}]
    (let [result (store/put-document metric commit-store id)]
      (log/info "new commit entry " url " " id " @ " (:time commit-info))
      result)))

(defn commit-entry-not-captured
  [store repo commit]
  (let [commit-info (git-query/commit-info repo commit)
        id          (:id commit-info)]
    (store/document-missing? store id)))

(defn collect-repo-commit-logs
  [store url path repository-id]
  (log/info "Collecting commit metrics for " url " master branch ")
  (let [repo (git/load-repo path)]
    (git/git-checkout repo "master")
    (let [commit-log  (git/git-log repo)
          new-commits (filter #(commit-entry-not-captured store repo %) commit-log)]
      (log/info "Found " (count new-commits) " New commits from " (count commit-log))
      (doseq [commit new-commits]
        (commit-and-diff store url repo commit repository-id)))))

(defn each-commit
  "Apply the function f to each version of the repository - based on the log"
  [f repo-path]
  (let [repo (git/load-repo repo-path)]
    (doseq [log (git/git-log repo)]
      (do (git/git-checkout repo (.getName log))
          (f repo-path (.getName log) (:time (git-query/commit-info repo log)))
          (git/git-checkout repo "master")))))

(defn get-config
  "Reads git configuration properties"
  [repo-path a b c]
  (let [repo (git/load-repo repo-path)]
    (.getString (.getConfig (.getRepository repo)) a b c)))

(defn commit-history
  ([repo]
   (let [log  (git/git-log repo)
         keys [:email :time :branches :changed_files :merge false :author :id :message]]
     (map (fn [c] (select-keys (git-query/commit-info repo c) keys)) log)))
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
  "Returns a list of repository commits grouped by day."
  [repo-path]
  (let [repo    (git/load-repo repo-path)
        master  (git/git-checkout repo "master")
        head    (git/git-checkout repo "HEAD")
        history (git/git-log repo)]
    (group-by #(t/with-time-at-start-of-day (tc/from-date (:time (git-query/commit-info repo %)))) history)))

(defn first-commit-by-day
  "Takes a list of commits grouped by day (as a list) and returns a single list containing the first commit from each day"
  [repo-path]
  (let [repo            (git/load-repo repo-path)
        history         (git/git-log repo)
        grouped-commits (group-by #(t/with-time-at-start-of-day (tc/from-date (:time (git-query/commit-info repo %)))) history)]
    (log/info "Found " (count history) " commits on " (count grouped-commits) " distinct days")
    (map #(-> % second first) grouped-commits)))

(defn commit-document-id
  "Computes a repeatable unique id based on the commit sha (name) with a postfix for a primary store key"
  [commit]
  (uuid-from (.getName commit) "cloc"))

(defn requires-collection
  "Checks to see if the given commit exists as a record in the given store. Avoids collecting code metrics multiple times."
  [code-mapping commit]
  (store/document-missing? code-mapping (commit-document-id commit)))

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

(defn clone-or-update [url local-dir]
  (if (directory? local-dir) (git-pull url local-dir) (git-clone url local-dir)))


(defn loc-to-code-doc [loc url repository-id timestamp]
  {:timestamp     timestamp
   :repository_id repository-id
   :metric        {:source    {:system :git :url url}
                   :type      :loc
                   :name      "TBD"
                   :collected (-> loc :metric :collected)
                   :activity  (-> loc :metric :activity)}
   :code          (-> loc :code)
   :collector     (-> loc :collector)})

(defn collect-repository-historical-code-metrics1
  [code-store workspace-path url repository-id]
  (log/info "Collect commits from workspace" workspace-path "url" url)
  (try+
    (let [repo-path (local-clone-path workspace-path url)]
      (clone-or-update url repo-path)
      (let [repo     (git/load-repo repo-path)
            commits  (first-commit-by-day repo-path)
            filtered (filter #(requires-collection code-store %) commits)]
        (log/info (count filtered) "days commits need collection")
        (doseq [commit filtered]
          (let [sha       (.getName commit)
                id        (commit-document-id commit)
                timestamp (:time (git-query/commit-info repo commit))]
            (git/git-checkout repo sha)
            (->
              repo-path
              (cloc/loc-collector)
              (loc-to-code-doc url repository-id timestamp)
              (store/put-document code-store id))))
        (git/git-checkout repo "master")))
    (catch Object _
      (log/error (:throwable &throw-context) "unexpected error collecting metrics for " url workspace-path))))



(defn collect-repository-historical-code-metrics
  [vcs-mapping code-mapping workspace url repository-id]
  (log/info "Collect commits workspace" workspace "url" url)
  (try+
    (let [local-dir (local-clone-path workspace url)]
      (log/info "Collecting " url " to " local-dir)
      (clone-or-update url local-dir)
      (each-commit-by-day
        (fn [path sha timestamp]
          (let [id (uuid-from sha "cloc")]
            (if (store/document-missing? code-mapping id)
              (cloc/loc-collector
                path
                (fn [a]
                  (log/info "code metrics " url " @ " timestamp)
                  (store/put-document {:timestamp     timestamp
                                       :repository_id repository-id
                                       :metric        {:source    {:system :git :url url}
                                                       :type      :loc
                                                       :name      "TBD"
                                                       :collected (-> a :metric :collected)
                                                       :activity  (-> a :metric :activity)}
                                       :code          (-> a :code)
                                       :collector     (-> a :collector)}
                                      code-mapping id))
                )
              (log/info "code metrics exist for " url " @ " timestamp)))

          ) local-dir))
    (catch Object _
      (log/error (:throwable &throw-context) "unexpected error collecting metrics for " url workspace))))


(defn collect-commit-logs
  [vcs-mapping code-mapping workspace url repository-id]
  (try+
    (let [local-dir (local-clone-path workspace url)]
      (log/info "Collecting commit logs from " local-dir "(" url ")")
      (clone-or-update url local-dir)
      (collect-repo-commit-logs vcs-mapping url local-dir repository-id))
    (catch Object _
      (log/error (:throwable &throw-context) "unexpected error collecting metrics for " url workspace))))
