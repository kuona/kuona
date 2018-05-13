(ns kuona-core.plantuml-test
  (:require [midje.sweet :refer :all])
  (:require [kuona-core.plantuml :as plantuml]))

(def test-src
  (str "@startuml\n" "Bob -> Alice : hello\n" "@enduml\n"))


(facts "about plantul"
       (fact "generates svg"
             (plantuml/generate-image test-src "test/testing.svg")))
