(ns github-crawler.core-test
  (:require [midje.sweet :refer :all]
            [github-crawler.core :refer :all]))

(facts "about url parameter building"
       (fact (url-parameter [:a :b]) => "a=b")
       (fact (url-parameter [:a 1]) => "a=1")
       (fact (url-parameter [:a "b"]) => "a=b")
       (fact (url-parameter ["a" 12]) => "a=12"))


(facts "about query string construction"
       (fact (query-string {:a :b :c :d}) => "a=b&c=d"))
