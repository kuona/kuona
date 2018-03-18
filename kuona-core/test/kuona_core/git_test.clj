(ns kuona-core.git-test
  (:require [midje.sweet :refer :all]
            [kuona-core.util :refer :all]
            [kuona-core.git :as git])
  (:import (java.net MalformedURLException URL)))

(facts "about commit history"
       (let [test-repo-path (clojure.string/join "/" [(canonical-path "..") "test" "test-repo"])]
         (fact "has commits"
               (> (count (git/commits test-repo-path))) => true)
         (fact "daily count is less than full count"
               (< (count (git/commit-by-day test-repo-path)) (count (git/commits test-repo-path))) => true)
         (fact "traversing the commit histry by day"
               (git/each-commit (fn [repo-path sha time]) test-repo-path) => nil)
         (fact "traversing the commit histry by day"
               (git/each-commit-by-day (fn [repo-path sha time]) test-repo-path) => nil)
         ))

(facts "about reading git configuration"
       (let [test-repo-path (clojure.string/join "/" [(canonical-path "..") "test" "test-repo"])]

         (fact "reading configured url returns valid url"
               (git/get-config test-repo-path "remote" "origin" "url") => (contains "functional-event-store"))))
