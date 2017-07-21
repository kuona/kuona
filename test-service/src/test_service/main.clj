(ns test-service.main
  (:require [test-service.handler :as service]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn -main
  [& args]
  (jetty/run-jetty #'service/app {:port 9100}))
