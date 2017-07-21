(ns jenkins-collector.config-test
  (:require [midje.sweet :refer :all]
            [jenkins-collector.config :refer :all]
            [cheshire.core :refer :all])
  (:import (java.io StringReader)))

(def empty-configuration "")

(def root-only-configuration
  (generate-string {:jenkins []}))


(def single-collection-configuration
  (generate-string
    {:jenkins [{:url         "http://example.com"
                :credentials {:username "uname"
                              :password "pword"}}]}))
(defn string-reader
  [s]
  (StringReader. s))


(def no-work-configuration {:collections []})

(def single-work-configuration {:collections [{:url         "http://example.com"
                                               :credentials {:username "uname"
                                                             :password "pword"}}]})

(facts "about configuration reading"
       (fact "empty configuration means no work"
             (load-config (string-reader empty-configuration)) => no-work-configuration)
       (fact "top level object with no children means no work"
             (load-config (string-reader root-only-configuration)) => no-work-configuration)
       (facts "supports single collection"
              (println single-collection-configuration)
              (load-config (string-reader single-collection-configuration)) => single-work-configuration))