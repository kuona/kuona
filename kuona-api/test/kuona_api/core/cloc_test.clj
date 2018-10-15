(ns kuona-api.core.cloc-test
  (:require [midje.sweet :refer :all]
            [kuona-api.core.cloc :refer :all]
            [kuona-api.core.git :refer :all]
            [kuona-api.core.store :as store]
            [kuona-api.core.stores :refer [code-metric-store]]
            [kuona-api.core.util :refer :all]))

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
  (fact (map-cloc-metric "foo" {}) => {:blanks   nil
                                       :code     nil
                                       :comments nil
                                       :files    nil
                                       :language "foo"})
  (fact (map-cloc-metric "foo" {:nFiles 10 :blank 20 :comment 30 :code 40}) => {:blanks 20, :code 40, :comments 30, :files 10, :language "foo"}))

(facts
  (fact (cloc-language-metric {:Clojure {:nFiles 10 :blank 20 :comment 30 :code 40}}) => (list {:language "clojure" :files 10, :blanks 20, :comments 30, :code 40})))


(facts "about as-activity"
       (let [r (as-activity (list {:language "clojure" :files 10, :blanks 20, :comments 30, :code 40}
                                  {:language "clojure" :files 11, :blanks 21, :comments 31, :code 41}) {:clojure {}})]
         (fact "names the collector"
               (-> r :collector :name) => :kuona-collector-cloc)
         (fact "contains the file count"
               (-> r :metric :activity :files) => 21)
         (fact "totals blank lines"
               (-> r :metric :activity :blanks) => 41)
         (fact "totals comment lines"
               (-> r :metric :activity :comments) => 61)
         (fact "totals code lines"
               (-> r :metric :activity :code) => 81)
         (fact "contains the details"
               (-> r :metric :activity :languages count) => 2)
         (fact "contains code details"
               r => (contains {:code {"clojure" {}}}))))

(facts
  (let [data {:header   {:cloc_url         "github.com/AlDanial/cloc",
                         :cloc_version     1.72,
                         :elapsed_seconds  0.0842750072479248,
                         :n_files          5,
                         :n_lines          163,
                         :files_per_second 59.3295706909965,
                         :lines_per_second 1934.14400452649},
              :Clojure  {:nFiles 3, :blank 18, :comment 20, :code 115},
              :Markdown {:nFiles 2, :blank 4, :comment 0, :code 6},
              :SUM      {:blank 22, :comment 20, :code 121, :nFiles 5}}]
    (fact "Removing header" (remove-cloc-header data) => {:Clojure  {:blank 18, :code 115, :comment 20, :nFiles 3},
                                                          :Markdown {:blank 4, :code 6, :comment 0, :nFiles 2}})
    (fact "mapping activity" (map (fn [x] (map-cloc-metric (first x) (second x))) (remove-cloc-header data)) = "")
    (fact "full trip"
          (let [activity (as-activity (map (fn [x] (map-cloc-metric (first x) (second x))) (remove-cloc-header data)) (remove-cloc-header data))]
            (-> activity :collector :name) => :kuona-collector-cloc
            (-> activity :metric :activity :languages count) => 2))))



(facts "about collecting cloc data for revisions"
       (fact "each-commit"
             (let [test-repo-path (clojure.string/join "/" [(canonical-path "..") "test" "test-repo"])]

               (each-commit (fn [path sha time]
                              (loc-collector path
                                (fn [a] store/put-document code-metric-store a (uuid-from sha "cloc")) ))
                            test-repo-path) => nil)))
