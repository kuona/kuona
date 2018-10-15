(ns kuona-api.core.cli-test
  (:require [midje.sweet :refer :all]
            [kuona-api.core.cli :as cli]))


(facts "about application configuration"
       (fact
         (cli/configure "test" [["-h" "--help" "Display this message and exit"]] ["-h"]) => "foo"
         (provided (cli/exit anything anything) => "foo"))

       (fact "exits with invalid option values"
             (cli/configure "test" [["-p" "--port PORT"]] ["-p"]) => "foo"
             (provided (cli/exit 1 "The following errors occurred while parsing your command:\n\nMissing required argument for \"-p PORT\"") => "foo"))
       (fact "returns map of valid parameters"
             (cli/configure "test" [["-p" "--port PORT"]] ["-p" "2010"]) => {:port "2010"}
             (cli/configure "test" [["-p" "--port PORT"]] ["--port" "2010"]) => {:port "2010"}))
