(ns kuona-core.collector.source-code-test
  (:require [midje.sweet :refer :all])
  (:require [kuona-core.collector.source-code :refer :all]))

(facts "about source code collection"

       (fact "scans repository"
             (collect nil "/Users/graham/projects/kuona-project" "someurl" 1)
             ))
