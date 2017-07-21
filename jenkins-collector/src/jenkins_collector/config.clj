(ns jenkins-collector.config
  (:require [cheshire.core :refer :all]))

(defn load-config
  [config-stream]
  (let [parsed (parse-stream config-stream true)]
    {:collections (into [] (:jenkins parsed))}))
