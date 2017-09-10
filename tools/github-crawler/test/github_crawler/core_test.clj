(ns github-crawler.core-test
  (:require [midje.sweet :refer :all]
            [github-crawler.core :refer :all]))

(facts "about cli arguments"
       (fact "arguments have defaults"
             (configure '()) => {:config    "properties.edn",
                                 :api-url   "http://dashboard.kuona.io",
                                 :force     false
                                 :workspace "/Volumes/data-drive/workspace",
                                 :page-file "page.edn",
                                 :languages []})
       (fact "loads configuration from file"
             (configure '("-c" "test/test-properties.edn")) => {:config        "test/test-properties.edn"
                                                                :client-id     "000"
                                                                :client-secret "111"
                                                                :api-url       "http://dashboard.kuona.io",
                                                                :force         false
                                                                :workspace     "/Volumes/data-drive/workspace",
                                                                :page-file     "page.edn",
                                                                :languages     []})
       (fact "cli arguments override configuration from file"
             (configure '("-c" "test/test-properties.edn" "-f")) => {:config        "test/test-properties.edn"
                                                                     :client-id     "000"
                                                                     :client-secret "111"
                                                                     :api-url       "http://dashboard.kuona.io",
                                                                     :force         true
                                                                     :workspace     "/Volumes/data-drive/workspace",
                                                                     :page-file     "page.edn",
                                                                     :languages     []}))
