(ns kuona-core.healthcheck-test
  (:require [midje.sweet :refer :all]
            [kuona-core.http :as http]))

(defn filter-actuator-links
  "Filters the list of actuator links for health and info references"
  [links]
  (merge {} (-> links
                :_links
                (select-keys [:info :health])))
  )
(defn merge-list
  "Merge the maps in the list into a single map"
  [list]
  (if (first list)
    (merge (first list) (merge-list (rest list)))
    {}))

(defn- read-link
  "reads the json response from the :href link"
  [link]
  (if (:href link)
    (http/json-get (:href link))
    {}))

(facts "about merging lists"
       (fact (merge-list []) => {})
       (fact (merge-list '()) => {})
       (fact (merge-list [{:foo :bar}]) => {:foo :bar})
       (fact (merge-list `({:foo :bar})) => {:foo :bar})
       (fact (merge-list [{:foo 1} {:bar 2}]) => {:foo 1 :bar 2}))


(defn spring-actuator-health
  "Given a Spring actuator endpoint returns the health and info endpoints as a single map."
  [url]
  (let [actuator-links (http/json-get url)
        links          (filter-actuator-links actuator-links)]
    (merge-list (map read-link (vals links)))))

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
