(ns github-crawler.crawler-test
  (:require [midje.sweet :refer :all]
            [github-crawler.crawler :as crawler]))

(facts "about url parameter building"
       (fact (crawler/url-parameter [:a :b]) => "a=b")
       (fact (crawler/url-parameter [:a 1]) => "a=1")
       (fact (crawler/url-parameter [:a "b"]) => "a=b")
       (fact (crawler/url-parameter ["a" 12]) => "a=12"))

(facts "about query string construction"
       (fact (crawler/query-string {:a :b :c :d}) => "a=b&c=d"))
