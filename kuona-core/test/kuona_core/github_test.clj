(ns kuona-core.github-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [clj-http.client :as http]
            [kuona-core.github :as github]))


(facts "about querying github api"
       (fact "reading a single repository"
             (github/get-project-repository "username", "repository") => (contains {:status :success})
             (provided (http/get "https://api.github.com/repos/username/repository") => {})))
