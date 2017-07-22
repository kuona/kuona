(ns kuona-collector.metric.store-test
  (:require [midje.sweet :refer :all]
            [clj-http.client :as http]
            [cheshire.core :refer :all]
            [kuona-collector.metric.store :as store]))

(facts
 (let [host "http://localhost:9200"]
 (fact (store/index :kuona-index host) => "http://localhost:9200/kuona-index")
 (fact (store/mapping :vcs (store/index :kuona-index host)) => "http://localhost:9200/kuona-index/vcs")
 (fact (store/put-document {} (store/mapping :vcs (store/index :kuona-index host)) 1) => (contains {:_id "1"}))
 (fact (store/get-document (store/mapping :vcs (store/index :kuona-index host)) 1) => (contains {:found true}))
 (fact (store/has-document? (store/mapping :vcs (store/index :kuona-index host)) 1) => true)
 (fact (store/has-index? (store/index :missing-index host)) => false)
 (fact (store/has-index? (store/index :kuona-index host)) =not=> {})
 (fact (store/create-index (store/index :kuona-test-index host) store/metric-mapping-type) =not=> {})
 (fact (store/has-index? (store/index :kuona-test-index host)) =not=> {})
 (fact (store/delete-index (store/index :kuona-test-index host)) =not=> {})))



(facts "pagination links"
       (fact (store/page-links #() :size 0 :count 0) => [])
       (fact (store/page-links #() :size 1 :count 0) => [])
       (fact (store/page-links #() :size 0 :count 1) => [])
       (fact (store/page-links #(str "page " %) :size 10 :count 10) => ["page 1"])
       (fact (store/page-links #(str "page " %) :size 10 :count 20) => ["page 1"
                                                                        "page 2"])
       (fact (store/page-links #(str "page " %) :size 10 :count 21) => ["page 1"
                                                                        "page 2"
                                                                        "page 3"]))

(facts "pagination"
       (fact (store/pagination-param :size 10 :page nil) => "size=10")
       (fact (store/pagination-param :size 10 :page 2) => "size=10&from=10")
       (fact (store/pagination-param :size 10 :page 3) => "size=10&from=20"))


(facts "search"
       (let [no-search-results {:body (generate-string {:hits {:total 0 :hits  []}})}
             expected-no-results {:count 0 :items '() :links []}]
       (fact (store/search "mapping" "term" 10 1 #(%)) => expected-no-results
             (provided
              (http/get anything anything) => no-search-results :times 1 ))
       (fact (store/search "mapping" "term" 10 1 #(%)) => expected-no-results
             (provided
              (http/get "mapping/_search?q=term&size=10" anything) => no-search-results :times 1 ))

       (fact (store/search "mapping" "term" 10 1 #(%)) => expected-no-results
             (provided
              (http/get "mapping/_search?q=term&size=10" anything) => no-search-results :times 1 ))
       (fact (store/search "mapping" "term" 10 2 #(%)) => expected-no-results
             (provided
              (http/get "mapping/_search?q=term&size=10&from=10" anything) => no-search-results :times 1 ))))
