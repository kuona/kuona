(ns kuona-api.config
  (:require [cheshire.core :refer :all]
            [slingshot.slingshot :refer [throw+]])
  (:gen-class))

(defn port-selector
  [params]
  (:api-service-port (:settings params)))

(defn missing-port?
  [params]
  (nil? (port-selector params)))

(defn es-selector
  [params]
  (:elasticsearch-url (:settings params)))

(defn missing-search?
  [params]
  (nil? (es-selector params)))

(defn load-config
  [config-stream]
  (let [params (parse-stream config-stream true)]
    (if (missing-port? params)
      (throw+ {:type ::missing-parameter :parameter ::port}))
    (if (missing-search? params)
      (throw+ {:type ::missing-parameter :parameter ::port}))
    {:port (port-selector params)
     :es-host (es-selector params)}))
