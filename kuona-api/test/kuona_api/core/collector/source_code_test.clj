(ns kuona-api.core.collector.source-code-test
  (:require [midje.sweet :refer :all])
  (:require [kuona-api.core.collector.source-code :refer :all]
            [kuona-api.core.util :as util]))

(facts "about source code collection"
       (fact "scans repository"
             (let [cwd (util/absolute-path ".")]
               (collect nil cwd "someurl" 1))))

