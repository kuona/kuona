(ns kuona-api.health-check-handlers-test
  (:require [midje.sweet :refer :all]
            [ring.util.response :as response]))

(defn new-check
  [check]
  {:error "HTTP health checks require a valid url"})

(defn find-health-check-reports-by-id
  "Queries the health check reports for a single health-check by ID or all health-checks by tag"
  ([])
  ([id]))

(defn find-health-check-reports-by-tags
  "Queries the health check reports that match the supplied tags"
  [tags])

(defn invalid-health-check-response
  [body]
  (-> body
      (response/response)
      (response/status 400)))

(future-facts "about defining health-checks"
       (fact "health checks can be tagged"
             (new-check {}) => {})
       (fact "can define multiple health checks in one call"
             (new-check {:health_checks []}) => (invalid-health-check-response {:error "No health checks found in request"}))
       (fact "http health-checks require an href"
             (new-check {:health_check {:type :http}}) => (invalid-health-check-response {:error "HTTP health checks require a valid url"})
             (new-check {:health_check {:type :http :href "http://some.invalid.host.local"}}) => (invalid-health-check-response {:error "Unable to resolve hostname 'some.invalid.host.local'"}))
       (fact "spring actuator health checks"
             (new-check {:health_check {:type :spring-actuator
                                        :href "http://localhost/actuator"}}) => (response/created {:href "/health-check/123"}))

       )
