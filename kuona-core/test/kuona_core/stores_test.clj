(ns kuona-core.stores-test
  (:require [midje.sweet :refer :all]
            [kuona-core.stores :as stores]))

(facts "about indexes"
       (fact (stores/es-index :foo {}) => {:name "foo"
                                           :type {}
                                           :url  "http://localhost:9200/foo"}))

(facts "about es urls"
       (let [test-index (stores/es-index :foo {})]
         (fact (stores/mapping-url test-index) => "http://localhost:9200/foo/_mapping")
         (fact (stores/count-url test-index) => "http://localhost:9200/foo/_count")
         (fact (stores/search-url test-index) => "http://localhost:9200/foo/_search")
         (fact (stores/search-url test-index {:term "bar"}) => "http://localhost:9200/foo/_search?q=bar")
         (fact (stores/search-url test-index {:term "bar" :page 1}) => "http://localhost:9200/foo/_search?q=bar")
         (fact (stores/search-url test-index {:term "bar" :page 1 :size 10}) => "http://localhost:9200/foo/_search?q=bar&size=10")
         (fact (stores/search-url test-index {:term "bar" :page 2 :size 10}) => "http://localhost:9200/foo/_search?q=bar&size=10&from=10")
         (fact (stores/id-url test-index 1) => "http://localhost:9200/foo/1")
         (fact (stores/update-url test-index 1) => "http://localhost:9200/foo/1/_update")

         )
       )
