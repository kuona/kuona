(ns environment-service.config-test
  (:require [midje.sweet :refer :all]
            [cheshire.core :refer :all]
            [environment-service.config :refer :all]))

(def valid-config
  (generate-string
   {:settings
    {:environment-service-port 9001
     :elasticsearch-url "http://localhost:9300"}}))

(def missing-port-config
  (generate-string {:settings
   {:elasticsearch-url "http://localhost:9300"}}))

(def missing-search-config
  (generate-string {:settings {:environment-service-port 9001 }}))

(defn string-reader
  [s]
  (java.io.StringReader. s))

(facts "about configuration"
       (fact "Copies the right values for valid configuration"
             (load-config (string-reader valid-config)) => {:port 9001
                                                            :es-host "http://localhost:9300" })
       (fact "something fails if the configuration is missing"
             (load-config (string-reader missing-port-config)) => (throws Exception))
       (fact "missing search throws excepton"
             (load-config (string-reader missing-search-config)) => (throws Exception)))
