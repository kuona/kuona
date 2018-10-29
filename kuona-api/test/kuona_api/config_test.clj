(ns kuona-api.config-test
  (:require [midje.sweet :refer :all]
            [cheshire.core :refer :all]
            [kuona-api.config :refer :all]
            [kuona-api.core.util :as util])
  (:import (java.io StringReader)))

(def valid-config
  (generate-string
   {:settings
    {:api-service-port 9001
     :elasticsearch-url "http://localhost:9300"}}))

(def missing-port-config
  (generate-string {:settings
   {:elasticsearch-url "http://localhost:9300"}}))

(def missing-search-config
  (generate-string {:settings {:api-service-port 9001 }}))

(facts "about configuration"
       (fact "Copies the right values for valid configuration"
             (load-config (util/string-reader valid-config)) => {:port 9001
                                                            :es-host   "http://localhost:9300" })
       (fact "something fails if the configuration is missing"
             (load-config (util/string-reader missing-port-config)) => (throws Exception))
       (fact "missing search throws exception"
             (load-config (util/string-reader missing-search-config)) => (throws Exception)))
