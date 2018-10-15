(ns kuona-api.core.healthcheck-test
  (:require [midje.sweet :refer :all]
            [kuona-api.core.http :as http]
            [kuona-api.core.healthcheck :refer :all]))


(facts "about merging lists"
       (fact (merge-list []) => {})
       (fact (merge-list '()) => {})
       (fact (merge-list [{:foo :bar}]) => {:foo :bar})
       (fact (merge-list `({:foo :bar})) => {:foo :bar})
       (fact (merge-list [{:foo 1} {:bar 2}]) => {:foo 1 :bar 2}))

(facts "about spring actuator filter"
       (fact (filter-actuator-links {}) => {})
       (fact (filter-actuator-links {:_links {:self {:href "foo"}}}) => {})
       (fact (filter-actuator-links {:_links {:info {:href "foo"}}}) => {:info {:href "foo"}})
       (fact (filter-actuator-links {:_links {:health {:href "foo"}}}) => {:health {:href "foo"}})
       (fact (filter-actuator-links {:_links {:health {:href "foo"} :info {:href "bar"}}}) => {:health {:href "foo"} :info {:href "bar"}}))

(facts "about reading links"
       (fact (read-link {:href "http://some.health.url"}) => "result"
             (provided (http/json-get "http://some.health.url") => "result")))

(facts "spring actuator health check checks"
       (fact "empty map if health and info are missing"
             (spring-actuator-health "http://some.url") => {}
             (provided (http/json-get "http://some.url") => {:_links {}}))
       (fact "result includes status"
             (spring-actuator-health "http://some.url") => {:status "DOWN"}
             (provided (http/json-get "http://some.url") => {:_links {:health {:href "http://some.health.url"}}})
             (provided (http/json-get "http://some.health.url") => {:status "DOWN"}))
       (fact "result includes status and info"
             (spring-actuator-health "http://some.url") => {:status "DOWN"
                                                            :info   "INFO"}
             (provided (http/json-get "http://some.url") => {:_links {:health {:href "http://some.health.url"}
                                                                      :info   {:href "http://some.info.url"}}})
             (provided (http/json-get "http://some.health.url") => {:status "DOWN"})
             (provided (http/json-get "http://some.info.url") => {:info "INFO"})))

(facts "about healthchecks"
       (fact "health checks requires a supported encoding"
             (health-check {}) => {:status      :failed
                                   :description "Unrecognised healthcheck encoding "}
             (health-check {:encoding :foo}) => {:status      :failed
                                                 :description "Unrecognised healthcheck encoding :foo"})
       (fact "reports unresolved hosts"
             (health-check {:encoding :json
                            :href     "http://some.rediculous.domain"}) => {:status      :failed
                                                                            :description "some.rediculous.domain: nodename nor servname provided, or not known"})
       (fact "reports unreachable hosts"
             (health-check {:encoding :json
                            :href     "http://localhost:9843"}) => {:status      :failed
                                                                    :description "Connection refused"})
       )
