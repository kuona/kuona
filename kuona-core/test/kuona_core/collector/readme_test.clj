(ns kuona-core.collector.readme-test
  (:require [midje.sweet :refer :all])
  (:require [kuona-core.collector.readme :as readme]))


(facts "about readme capture"
       (fact "returns not found if readme not found"
             (readme/collect "./missing") => {:readme {:found false
                                               :text  ""}})
       (fact "reads manifext in root directory"
             (readme/collect "..") => {:readme {:found true
                                                :text  (slurp "../README.md")}}))
