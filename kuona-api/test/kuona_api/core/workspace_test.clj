(ns kuona-api.core.workspace-test
  (:require [midje.sweet :refer :all])
  (:require [kuona-api.core.workspace :refer :all]))

(facts "about the workspace"
       (fact "default value is nil"
             (reset-workspace-path!) => nil
             (get-workspace-path) => nil)
       (fact "can set the workspace path"
             (set-workspace-path "foobar") => "foobar"
             (get-workspace-path) => "foobar")
       (fact "only valid paths are acceptable"
             (reset-workspace-path!) => nil
             (workspace-path-valid?) => false)
       (fact "only valid paths are acceptable"
             (set-workspace-path "test") => "test"
             (workspace-path-valid?) => true))

