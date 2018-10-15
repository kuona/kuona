(ns kuona-api.core.query-test
  (:require [midje.sweet :refer :all]
            [kuona-api.core.query :as query]))


(facts "about query parsing"
       (fact "term query"
             (query/parse "term") => {:terms '("term")}
             )
       (fact "accepts multiple terms"
             (query/parse "term1 term2 term3") => {:terms '("term1" "term2" "term3")}
             ))

(facts "about query conversion"
       (fact "term forms simple text query"
             (query/generate "cat") => {"simple_query_string" {"query" "cat"}})
       (fact "terms form simple text query"
             (query/generate "cat dog") => {"simple_query_string" {"query" "cat dog"}}))
