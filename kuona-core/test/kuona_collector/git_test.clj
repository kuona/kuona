(ns kuona-collector.git-test
  (:require [midje.sweet :refer :all]
            [kuona-collector.util :refer :all]
            [kuona-collector.git :as git]))

;"/Users/graham/workspace/d0ed12f3-096a-30c0-83b2-b2ef29bb40d7"
(facts "about commit history"
       (let [test-repo-path (clojure.string/join "/" [(canonical-path ".") "test-repo"])]
         (println test-repo-path)
         (fact "has commits"
               (> (count (git/commits test-repo-path))) => true)
         (fact "daily count is less than full count"
               (< (count (git/commit-by-day test-repo-path)) (count (git/commits test-repo-path))) => true)
         (fact "traversing the commit histry by day"
               (git/each-commit (fn [repo-path sha time] ) test-repo-path) => nil)
         (fact "traversing the commit histry by day"
               (git/each-commit-by-day (fn [repo-path sha time] ) test-repo-path) => nil)))
