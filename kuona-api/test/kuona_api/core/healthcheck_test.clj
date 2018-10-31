(ns kuona-api.core.healthcheck-test
  (:require [midje.sweet :refer :all]
            [kuona-api.core.http :as http]
            [kuona-api.core.healthcheck :refer :all]
            [kuona-api.core.util :as util]))


(facts "about merging lists of maps"
       (fact (merge-map-list []) => {})
       (fact (merge-map-list '()) => {})
       (fact (merge-map-list [{:foo :bar}]) => {:foo :bar})
       (fact (merge-map-list `({:foo :bar})) => {:foo :bar})
       (fact (merge-map-list [{:foo 1} {:bar 2}]) => {:foo 1 :bar 2}))

(facts "about spring actuator filter"
       (fact (filter-actuator-links {}) => {})
       (fact (filter-actuator-links {:_links {:self {:href "foo"}}}) => {})
       (fact (filter-actuator-links {:_links {:info {:href "foo"}}}) => {:info {:href "foo"}})
       (fact (filter-actuator-links {:_links {:health {:href "foo"}}}) => {:health {:href "foo"}})
       (fact (filter-actuator-links {:_links {:health {:href "foo"} :info {:href "bar"}}}) => {:health {:href "foo"} :info {:href "bar"}}))

(facts "about reading links"
       (fact (read-href-link {}) => {})
       (fact (read-href-link {:href "http://some.health.url"}) => "result"
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

(facts "about health checks"
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
                                                                    :description "Connection refused"}))

(facts "about health check log entries"
       (fact "log contains the health check id"
             (let [hc              {:id (util/uuid)}
                   health          {:status :ok}
                   collection-date (util/timestamp)]
               (health-check-log hc, health collection-date) => (contains {:health_check_id (-> hc :id)})
               (health-check-log hc, health collection-date) => (contains {:date collection-date})

               )))


(facts "about health check snapshots"
       (fact "snapshot contains the health check with id and date"
             (let [hc-id   (util/uuid)
                   hc-date (util/timestamp)
                   hc-tags [1 2]
                   hc      {:id   hc-id
                            :tags hc-tags
                            :type :health-check-type}
                   hc-log  {:foo    :should-not-be-in-result
                            :health :should-be-there}
                   hc-logs [hc-log]]
               (health-check-snapshot hc-logs hc hc-date) => (contains {:id hc-id})
               (health-check-snapshot hc-logs hc hc-date) => (contains {:date hc-date})
               (health-check-snapshot hc-logs hc hc-date) => (contains {:tags hc-tags})
               (health-check-snapshot hc-logs hc hc-date) => (contains {:type :health-check-type})
               (health-check-snapshot hc-logs hc hc-date) => (contains {:results [{:health :should-be-there}]}))

             ))

(facts "about health check handler selection"
       (fact "matches HTTP check"
             (health-check-fn :HTTP_GET) => (exactly perform-http-health-checks))
       (fact
         (health-check-fn :SPRING_ACTUATOR) => (exactly perform-spring-actuator-health-check))
       (fact
         (health-check-fn :FOO) => (exactly perform-health-check-error)))

(facts "about valid endpoints"
       (fact (valid-endpoint? "") => false)
       (fact (valid-endpoint? "http://google.com") => true))

(facts "about valid health checks"
       (let [invalid-response {:valid       false
                               :description "Health checks require a type (HTTP_GET, SPRING_ACTUATOR. A list of one or more tags and a list of endpoints to check"}]
         (fact "empty check definition is not valid"
               (valid-health-check? {}) => invalid-response)
         (fact "requires tags and endpoints"
               (valid-health-check? {:type "HTTP_GET"}) => invalid-response)
         (fact "endpoints must be valid urls"
               (valid-health-check? {:type      "HTTP_GET"
                                     :tags      ["foo"]
                                     :endpoints ["http"]}) => invalid-response))

       (fact "valid health check requires type, tags and endpoints"
             (valid-health-check? {:type      "HTTP_GET"
                                   :tags      ["foo"]
                                   :endpoints ["http://foo.com"]}) => {:valid true}))
