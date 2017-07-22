(ns test-service.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [resource-response response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.adapter.jetty :as jetty]))

(defroutes app-routes
  (GET "/" [] (response {:name "Test service" :links [
                                                      {:href "/health" :rel :health}
                                                      {:href "/info" :rel :info}
                                                      {:href "/status" :rel :status}
                                                      {:href "/status/up" :rel :up-status}
                                                      {:href "/status/down" :rel :down-status}
                                                      ]}))
  (GET "/health" [] (response { :build { :artifact "test-service"
                                        :name "test-service"
                                        :description "Kuona service for HTTP collector testing"
                                        :version "0.0.1-SNAPSHOT" }}))
  (GET "/info" [] (response { :build { :artifact "test-service"
                                        :name "test-service"
                                        :description "Kuona service for HTTP collector testing"
                                        :version "0.0.1-SNAPSHOT" }}))
  (GET "/status" [] (response {:status "UP"
                              :diskSpace { :status "UP"
                                          :total 250656219136
                                          :free 190185013248
                                          :threshold 10485760 }
                              :hystrix {:status "UP"}
                              }))
  (GET "/status/up" [] (response {:status "UP"
                              :diskSpace { :status "UP"
                                          :total 250656219136
                                          :free 190185013248
                                          :threshold 10485760 }
                              :hystrix {:status "UP"}
                              }))
  (GET "/status/down" [] (response {:status "DOWN"
                              :diskSpace { :status "UP"
                                          :total 250656219136
                                          :free 190185013248
                                          :threshold 10485760 }
                              :hystrix {:status "UP"}
                              }))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)
      (wrap-defaults api-defaults)))
