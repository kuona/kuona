(ns kuona-core.cloc
  (:require [clojure.java.shell :as shell]
            [cheshire.core :refer :all]
            [clojure.tools.logging :as log]
            [kuona-core.util :refer :all])
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
  (filter #(pom-file? (.getPath %)) files))

(defn sum-count
  [d k]
  (reduce + (map #(k %) d))) 

(defn add-metadata
  [metrics]
  {:loc {:total     {:file-count    (sum-count metrics :file-count)
                     :comment-lines (sum-count metrics :comment-lines)
                     :blank-lines   (sum-count metrics :blank-lines)
                     :code-lines    (sum-count metrics :code-lines)}
         :languages metrics}})

;(defn add-metadata
;  [metrics]
;  {:timestamp (timestamp)
;   :metric    {:type      :loc
;               :name      "TBD"
;               :source    {:system :git
;                           :url    "TBD"}
;               :activity  {:file-count    (sum-count metrics :file-count)
;                           :comment-lines (sum-count metrics :comment-lines)
;                           :blank-lines   (sum-count metrics :blank-lines)
;                           :code-lines    (sum-count metrics :code-lines)
 ;                          :languages     metrics}
 ;;              :collected (timestamp)}
 ;  :collector {:name    :kuona-code-collector
;               :version "0.1"}})

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
  (filter #(interesting-file? (.getPath %)) files))


(defn map-cloc-metric
  [language d]
  {:language      (language-name language)
   :file-count    (:nFiles d)
   :blank-lines   (:blank d)
   :comment-lines (:comment d)
   :code-lines    (:code d)})

(defn cloc-language-metric
  [entry]
  (map-cloc-metric (first entry) (second entry)))

(defn remove-cloc-header
  [data]
  (let [header-keys #{:header "header" :SUM "SUM"}
        keys (keys data)
        data-keys (filter (fn [x] (not (contains? header-keys x))) keys)]
    (into {} (map (fn [k] {k (get data k)}) data-keys))))

(defn cloc
  [path]
  (let [result (shell/sh "cloc" "--json" path) ]
    (parse-string (-> result :out) true)))

(defn maven-loc-module
  "Compute line of code metrics for the module. Separate results into production and test code"
  [root pom-path]
  (let [module-path (.getParent (clojure.java.io/file pom-path))
        main-path   (clojure.string/join "/" [module-path "src" "main"])
        test-path   (clojure.string/join "/" [module-path "src" "test"])
        key         (root-relative-path  root module-path)]
    {key {:production (add-metadata (map cloc-language-metric (remove-cloc-header (cloc main-path))))
          :test       (add-metadata (map cloc-language-metric (remove-cloc-header (cloc test-path))))}}
     ))

(defn module-summary
  [details]
  (let [production-code (reduce + (map #(-> (second %) :production :loc :total :code-lines) details))
        test-code       (reduce + (map #(-> (second %) :test :loc :total :code-lines) details))]
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
  (map (fn [key] (map-cloc-metric key (get entry key)))  (keys entry)))

(defn as-activity
  [metrics]
  {:metric    {:collected (timestamp)
               :activity  {:file-count    (sum-count metrics :file-count)
                           :comment-lines (sum-count metrics :comment-lines)
                           :blank-lines   (sum-count metrics :blank-lines)
                           :code-lines    (sum-count metrics :code-lines)
                           :languages     metrics}}
   :collector {:name    :kuona-collector-cloc
               :version "0.1"}})

(defn collect-loc
  [path]
  (as-activity (map (fn [x] (map-cloc-metric (first x) (second x))) (remove-cloc-header (cloc path)))))

(defn loc-collector
  [f path sha]
  (log/info "loc-collector " path sha)
  (f (collect-loc path)))

