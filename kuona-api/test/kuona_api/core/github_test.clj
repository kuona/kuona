(ns kuona-api.core.github-test
  (:require [clojure.test :refer :all]
            [slingshot.slingshot :refer :all]
            [midje.sweet :refer :all]
            [clj-http.client :as http]
            [kuona-api.core.github :as github]))

(facts "about querying github api"
       (fact "reading a single repository"
             (github/get-project-repository "username" "repository") => (contains {:status :success})
             (provided (http/get "https://api.github.com/repos/username/repository" anything) => {}))

       (fact "reading project repositories"
             (github/get-project-repositories "kuona") => (contains {:status :success :github []})
             (provided (http/get "https://api.github.com/users/kuona/repos" anything) => {:body "[]"})))
