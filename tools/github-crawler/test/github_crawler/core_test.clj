(ns github-crawler.core-test
  (:require [midje.sweet :refer :all]
            [kuona-api.core.cli :as cli]
            [github-crawler.core :refer :all]))

(facts "about cli arguments"
       (fact "arguments have defaults"
             (cli/configure "foo" cli-options '()) => {:config    "properties.edn",
                                                       :api-url   "http://dashboard.kuona.io",
                                                       :force     false
                                                       :workspace "/Volumes/data-drive/workspace",
                                                       :page-file "page.edn"})
       (fact "loads configuration from file"
             (cli/configure "foo" cli-options '("-c" "test/test-properties.edn")) => {:config        "test/test-properties.edn"
                                                                                      :client-id     "000"
                                                                                      :client-secret "111"
                                                                                      :api-url       "http://dashboard.kuona.io",
                                                                                      :force         false
                                                                                      :workspace     "/Volumes/data-drive/workspace",
                                                                                      :page-file     "page.edn",
                                                                                      :languages     ["java" "javascript" "kotlin"]})
       (fact "cli arguments override configuration from file"
             (cli/configure "foo" cli-options '("-c" "test/test-properties.edn" "-f")) => {:config        "test/test-properties.edn"
                                                                                           :client-id     "000"
                                                                                           :client-secret "111"
                                                                                           :api-url       "http://dashboard.kuona.io",
                                                                                           :force         true
                                                                                           :workspace     "/Volumes/data-drive/workspace",
                                                                                           :page-file     "page.edn",
                                                                                           :languages     ["java" "javascript" "kotlin"]})
       (fact "cli arguments override configuration from file"
             (cli/configure "foo" cli-options '("-c" "test/test-properties.edn" "-f")) => (contains {:force true})))
