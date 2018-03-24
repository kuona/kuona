(ns kuona-core.query-test
  (:require [midje.sweet :refer :all]
            [kuona-core.query :as query]))


(facts "about query parsing"
       (fact "term query"
             (query/parse "term") => {:terms '("term")}
             )
       (fact "accepts multiple terms"
             (query/parse "term1 term2 term3") => {:terms '("term1" "term2" "term3")}
             ))
