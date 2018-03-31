(ns tfs-crawler.crawler-test
  (:require [midje.sweet :refer :all]
            [tfs-crawler.crawler :as crawler]))

(facts "about visual studio API urls"
       (fact "contains the organisation name"
             (crawler/vs-url "foo") => "https://foo.visualstudio.com/_apis/git/repositories?api-version=4.1"))

