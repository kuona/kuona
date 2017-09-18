(ns snapshot-collector.core-test
  (:require [midje.sweet :refer :all]
            [kuona-core.cli :as cli]
            [snapshot-collector.core :refer :all]))


(facts "about command line interface"
       (fact "uses defaults if no arguments supplied"
             (cli/validate-args "snap" cli-options []) => {:api-url   "http://dashboard.kuona.io"
                                                           :force     false
                                                           :workspace "/Volumes/data-drive/workspace"})
       (fact "can supply short parameters"
             (cli/validate-args "snap" cli-options  ["-a" "http://localhost" "-f" "-w" "/some/path"]) => {:api-url   "http://localhost"
                                                                                                          :force     true
                                                                                                          :workspace "/some/path"})
       (fact "can supply long parameters"
             (cli/validate-args "snap" cli-options ["--api-url" "http://localhost" "--force" "--workspace" "/some/path"]) => {:api-url   "http://localhost"
                                                                                                                              :force     true
                                                                                                                              :workspace "/some/path"})
       (fact "invalid options produce error"
             (cli/validate-args "snap" cli-options ["--foo"]) => {:exit-message "The following errors occurred while parsing your command:\n\nUnknown option: \"--foo\""}
             (:ok? (cli/validate-args "snap" cli-options ["--foo"])) => nil)
       (fact "help support"
             (:ok? (cli/validate-args "snap" cli-options ["-h"])) => true
             (contains? (cli/validate-args "snap" cli-options ["-h"]) :exit-message) => true))
