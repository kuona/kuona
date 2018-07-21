(ns kuona-core.store-test
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http]
            [kuona-core.store :as store]
            [midje.sweet :refer :all])
  (:import (kuona_core.stores DataStore)))

(facts "about elasticsearch mapping to ktypes"
       (fact "empty mapping is empty"
             (store/es-mapping-to-ktypes {}) => {})
       (fact "maps single entry"
             (store/es-mapping-to-ktypes {:collected {:type "date"}}) => {:collected :timestamp})
       (fact "maps single entry"
             (store/es-mapping-to-ktypes {:collected {:type "date"} :size {:type "long"}}) => {:collected :timestamp :size :long}))

(facts "about elastic search type mapping"
       (fact "maps date property type to timestamp"
             (store/es-type-to-ktype :collected {:type "date"}) => {:collected :timestamp})
       (fact "maps multi valued objects as object type"
             (store/es-type-to-ktype :collector {:properties {}}) => {:collector :object})
       (fact "maps long valued objects to integer"
             (store/es-type-to-ktype :collector {:type "long"}) => {:collector :long})
       (fact "maps keyword valued objects to string"
             (store/es-type-to-ktype :collector {:type "keyword"}) => {:collector :string})
       (fact "maps object to object"
             (store/es-type-to-ktype :collector {:type "object"}) => {:collector :object}))





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
       (fact (store/pagination-param :size 10 :page 3) => "size=10&from=20")
       (fact (store/pagination-param :size 10 :page "3") => "size=10&from=20"))


(def test-store (DataStore. :test :tests {}))


(facts "search"
       (let [no-search-results   {:body (generate-string {:hits {:total 0 :hits []}})}
             expected-no-results {:count 0 :items '() :links []}]
         (fact (store/search test-store "term" 10 1 #(%)) => expected-no-results
               (provided
                 (http/get "http://localhost:9200/kuona-test/tests/_search?q=term&size=10" anything) => no-search-results :times 1))
         (fact (store/search test-store "term" 10 2 #(%)) => expected-no-results
               (provided
                 (http/get "http://localhost:9200/kuona-test/tests/_search?q=term&size=10&from=10" anything) => no-search-results :times 1))))

(def parsing-exception
  {:error  {:root_cause [{:type   :parsing_exception
                          :reason "[match_all] malformed query, expected [END_OBJECT] but found [FIELD_NAME]"
                          :line   1
                          :col    73}]
            :type       :parsing_exception
            :reason     "[match_all] malformed query, expected [END_OBJECT] but found [FIELD_NAME]"
            :line       1
            :col        73}
   :status 400})

(facts "about elasticsearch error handling"
       (fact "maps parsing exception"
             (-> (store/es-error parsing-exception) :error :type) => :parsing_exception)
       (fact "composes description"
             (-> (store/es-error parsing-exception) :error :description) => "[match_all] malformed query, expected [END_OBJECT] but found [FIELD_NAME] line 1 column 73")
       (fact "maps parsing location"
             (-> (store/es-error parsing-exception) :error :query_location) => {:line 1 :col 73})
       (fact "handles query parsing errors"
             (store/es-error parsing-exception) => {:error {:type           :parsing_exception
                                                            :description    "[match_all] malformed query, expected [END_OBJECT] but found [FIELD_NAME] line 1 column 73"
                                                            :query_location {:line 1 :col 73}}}))
