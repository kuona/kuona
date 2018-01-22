(ns jenkins-collector.config-test
  (:require [midje.sweet :refer :all]
            [jenkins-collector.config :refer :all]
            [cheshire.core :refer :all])
  (:import (java.io StringReader)))

(defn as-json-stream
  "Returns a stream containing the supplied parxameter as JSON text"
  [x]
  (->
    x
    generate-string
    StringReader.))

(facts "about reading configuration from streams"
       (fact "empty configuration means no work"
             (let [empty-configuration-stream (as-json-stream "")]
               (load-config empty-configuration-stream) => {:collections []}))
       (fact "top level object with no children means no work"
             (let [root-only-configuration-stream (as-json-stream {:jenkins []})]
               (load-config root-only-configuration-stream) => {:collections []}))
       (facts "supports single collection"
              (let [single-collection-configuration-stream (as-json-stream {:jenkins [{:url         "http://example.com"
                                                                                       :credentials {:username "uname"
                                                                                                     :password "pword"}}]})]
                (load-config single-collection-configuration-stream) => {:collections [{:url         "http://example.com"
                                                                                        :credentials {:username "uname"
                                                                                                      :password "pword"}}]})))
