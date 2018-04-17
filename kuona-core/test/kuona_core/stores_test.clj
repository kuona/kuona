(ns kuona-core.stores-test
  (:require [midje.sweet :refer :all]
            [kuona-core.stores :as stores]
            [kuona-core.metric.store :as store]))

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

;(facts
;  (let [host "http://localhost:9200"]
;    (fact (stores/has-index? (stores/index :missing-index host)) => false)
;    (fact (stores/has-index? (stores/index :kuona-index host)) =not=> {})
;    (fact (stores/create-index (stores/index :kuona-test-index host) store/metric-mapping-type) =not=> {})
;    (fact (stores/has-index? (stores/index :kuona-test-index host)) =not=> {})
;    (fact (stores/delete-index (stores/index :kuona-test-index host)) =not=> {})))
