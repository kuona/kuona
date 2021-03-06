(ns kuona-api.core.cloc
  (:require [clojure.java.shell :as shell]
            [clojure.set :refer [rename-keys]]
            [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [kuona-api.core.util :refer :all]
            [kuona-api.core.util :as util])
  (:gen-class))

(defn clojure-project-file?
  "Given a file path returns the path for pom files or nil if not a POM file."
  [path]
  (re-matches #".*/project.clj" path))

(defn pom-file?
  "Given a file path returns the path for pom files or nil if not a POM file."
  [path]
  (re-matches #".*/pom.xml" path))

(defn pom-files
  "Given a path returns a sequence of all the POM files."
  [files]
  (filter #(pom-file? (util/get-path %)) files))

(defn sum-count
  [d k]
  (reduce + (map #(k %) d)))

(defn add-metadata
  [metrics]
  {:loc {:total     {:files    (sum-count metrics :files)
                     :comments (sum-count metrics :comments)
                     :blanks   (sum-count metrics :blanks)
                     :code     (sum-count metrics :code)}
         :languages metrics}})

(defn root-relative-path
  [root path]
  (let [relative-path (subs path (count root))]
    (if (= "" relative-path) "/" relative-path)))

(defn language-name
  [s]
  (clojure.string/replace (clojure.string/lower-case (name s)) #" " "_"))

(defn interesting-file?
  "Returns true if the file is interesting and false otherwise. All
  files are interesting unless they match the exclusions list."
  [path]
  (let [exclusions [#"target/" #".DS_Store"]]
    (not (reduce (fn [a b] (or a b)) (map #(re-find % path) exclusions)))))

(defn interesting-files
  "Filters the supplied list of file paths based on our interest"
  [files]
  (filter #(interesting-file? (util/get-path %)) files))


(defn map-cloc-metric
  [language d]
  {:language (language-name language)
   :files    (:nFiles d)
   :blanks   (:blank d)
   :comments (:comment d)
   :code     (:code d)})

(defn cloc-language-metric
  [entry]
  (map-cloc-metric (first entry) (second entry)))

(defn remove-cloc-header
  [data]
  (let [header-keys #{:header "header" :SUM "SUM"}
        keys        (keys data)
        data-keys   (filter (fn [x] (not (contains? header-keys x))) keys)]
    (into {} (map (fn [k] {k (get data k)}) data-keys))))

(defn cloc
  [path]
  (let [result (shell/sh "cloc" "--json" path)]
    (parse-string (-> result :out) true)))

(defn maven-loc-module
  "Compute line of code metrics for the module. Separate results into production and test code"
  [root pom-path]
  (let [module-path (.getParent (clojure.java.io/file pom-path))
        main-path   (clojure.string/join "/" [module-path "src" "main"])
        test-path   (clojure.string/join "/" [module-path "src" "test"])
        key         (root-relative-path root module-path)]
    {key {:production (add-metadata (map cloc-language-metric (remove-cloc-header (cloc main-path))))
          :test       (add-metadata (map cloc-language-metric (remove-cloc-header (cloc test-path))))}}
    ))

(defn module-summary
  [details]
  (let [production-code (reduce + (map #(-> (second %) :production :loc :total :code) details))
        test-code       (reduce + (map #(-> (second %) :test :loc :total :code) details))]
    {:summary {:production   production-code
               :test         test-code
               :ratio        (float (/ production-code test-code))
               :module-count (count details)}}))

(defn module-scan
  [path]
  (let [files         (interesting-files (find-files path))
        maven-modules (pom-files files)
        metrics       (into {} (map #(maven-loc-module path %) maven-modules))]
    (merge (module-summary metrics) {:modules metrics})))


(defn cloc-language-metric
  [entry]
  (map (fn [key] (map-cloc-metric key (get entry key))) (keys entry)))

(defn as-activity
  [metrics original]
  {:metric    {:collected (timestamp)
               :activity  {:files     (sum-count metrics :files)
                           :comments  (sum-count metrics :comments)
                           :blanks    (sum-count metrics :blanks)
                           :code      (sum-count metrics :code)
                           :languages metrics}}
   :code      (into {} (map (fn [[k v]] {(language-name k) (rename-keys v {:nFiles :files :blank :blanks :comment :comments :code :code})}) original))
   :collector {:name    :kuona-collector-cloc
               :version "0.2"}})

(defn collect-loc
  [path]
  (let [cleaned-metrics (remove-cloc-header (cloc path))]
    (as-activity (map (fn [x] (map-cloc-metric (first x) (second x))) cleaned-metrics) cleaned-metrics)))

(defn loc-collector
  ([path]
   (log/info "loc-collector " path)
   (collect-loc path))
  ([path f]
   (log/info "loc-collector " path)
   (f (collect-loc path))))
