(ns kuona-core.util-test
  (:require [midje.sweet :refer :all]
            [kuona-core.util :refer :all]))


(facts "about uuids"
       (fact "uuids are unique"
             (uuid) =not=> (uuid))
       (fact "uuids based on same data are the same"
             (uuid-from "foo") => (uuid-from "foo")))

(facts "about directories"
       (fact "test is a directory"
             (directory? "test") => true)
       (fact "util_test.clj is not a directory"
             (directory? "test/util_test.clj") => false))


