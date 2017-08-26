(ns snapshot-collector.core-test
  (:require [midje.sweet :refer :all]
            [snapshot-collector.core :refer :all]))


(facts "about command line interface"
       (fact "uses defaults if no arguments supplied"
             (validate-args []) => {:api-url   "http://dashboard.kuona.io"
                                    :force     false
                                    :workspace "/Volumes/data-drive/workspace"})
       (fact "can supply short parameters"
             (validate-args ["-a" "http://localhost" "-f" "-w" "/some/path"]) => {:api-url   "http://localhost"
                                                                                  :force     true
                                                                                  :workspace "/some/path"})
       (fact "can supply long parameters"
             (validate-args ["--api-url" "http://localhost" "--force" "--workspace" "/some/path"]) => {:api-url   "http://localhost"
                                                                                                       :force     true
                                                                                                       :workspace "/some/path"})
       (fact "invalid options produce error"
             (validate-args ["--foo"]) => {:exit-message "The following errors occurred while parsing your command:\n\nUnknown option: \"--foo\""}
             (:ok? (validate-args ["--foo"])) => nil)
       (fact "help support"
             (:ok? (validate-args ["-h"])) => true
             (contains? (validate-args ["-h"]) :exit-message) => true))
