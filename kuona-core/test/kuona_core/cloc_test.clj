(ns kuona-core.cloc-test
  (:require [midje.sweet :refer :all]
            [kuona-core.cloc :refer :all]
            [kuona-core.git :refer :all]
            [kuona-core.metric.store :as store]
            [kuona-core.util :refer :all]))

(facts "about language name"
       (fact "can be a symbol" (language-name :foo) => "foo")
       (fact "is lowercase" (language-name "FOO") => "foo")
       (fact "has no white spacce" (language-name "foo bar") => "foo_bar"))

(facts
 (fact (remove-cloc-header {:header :foo}) => {})
 (fact (remove-cloc-header {"header" :foo}) => {})
 (fact (remove-cloc-header {"SUM" :foo}) => {})
 (fact (remove-cloc-header {:SUM :foo}) => {})
 (fact (remove-cloc-header {:header :foo :clojure :rocks}) => {:clojure :rocks}))

(facts
 (fact (map-cloc-metric "foo" {}) => {:blank-lines nil
                                      :code-lines nil
                                      :comment-lines nil
                                      :file-count nil
                                      :language "foo"})
 (fact (map-cloc-metric "foo" {:nFiles 10 :blank 20 :comment 30 :code 40}) => {:blank-lines 20, :code-lines 40, :comment-lines 30, :file-count 10, :language "foo"}))

(facts
 (fact (cloc-language-metric {:Clojure {:nFiles 10 :blank 20 :comment 30 :code 40}}) => (list {:language "clojure" :file-count 10, :blank-lines 20, :comment-lines 30, :code-lines 40})))


(facts "about as-activity"
       (let [r (as-activity (list {:language "clojure" :file-count 10, :blank-lines 20, :comment-lines 30, :code-lines 40}
                                  {:language "clojure" :file-count 10, :blank-lines 20, :comment-lines 30, :code-lines 40}))]
         (fact "names the collector"
               (-> r :collector :name) => :kuona-collector-cloc)
         (fact "contains the file count"
               (-> r :metric :activity :file-count ) => 20)
         (fact "totals blank lines"
               (-> r :metric :activity :blank-lines ) => 40)
         (fact "totals comment lines"
               (-> r :metric :activity :comment-lines ) => 60)
         (fact "totals code lines"
               (-> r :metric :activity :code-lines ) => 80)
         (fact "contains the details"
               (-> r :metric :activity :languages count) => 2)))

(facts
 (let [data {:header {:cloc_url "github.com/AlDanial/cloc",
                      :cloc_version 1.72,
                      :elapsed_seconds 0.0842750072479248,
                      :n_files 5,
                      :n_lines 163,
                      :files_per_second 59.3295706909965,
                      :lines_per_second 1934.14400452649},
             :Clojure {:nFiles 3, :blank 18, :comment 20, :code 115},
             :Markdown {:nFiles 2, :blank 4, :comment 0, :code 6},
             :SUM {:blank 22, :comment 20, :code 121, :nFiles 5}}]
   (fact "Removing header" (remove-cloc-header data) => {:Clojure {:blank 18, :code 115, :comment 20, :nFiles 3},
                                                         :Markdown {:blank 4, :code 6, :comment 0, :nFiles 2}})
   (fact "mapping activity" (map (fn [x] (map-cloc-metric (first x) (second x))) (remove-cloc-header data)) = "")  
   (fact "full trip"
         (let [activity (as-activity (map (fn [x] (map-cloc-metric (first x) (second x))) (remove-cloc-header data)))]
           (-> activity :collector :name) => :kuona-collector-cloc
           (-> activity :metric :activity :languages count) => 2))))



(facts "about collecting cloc data for revisions"
       (fact "each-commit"
             (let [index          (store/index :kuona-metrics "http://localhost:9200")
                   code-mapping   (store/mapping :code index)
                   test-repo-path (clojure.string/join "/" [(canonical-path ".") "test-repo"])]
               
               (each-commit (fn [path sha time]
                              (loc-collector
                                (fn [a] store/put-document code-mapping a (uuid-from sha "cloc")) path sha))
                            test-repo-path) => nil)))
