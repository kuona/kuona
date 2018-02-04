(ns kuona-core.github-test
  (:require [clojure.test :refer :all]
            [slingshot.slingshot :refer :all]
            [midje.sweet :refer :all]
            [clj-http.client :as http]
            [kuona-core.github :as github]))

(facts "about http exception handling"
       (fact (github/wrap-http-call (fn [] true)) => true)

       (fact "400" (github/wrap-http-call (fn [] (throw+ {:status 400 :request-time 0 :headers {} :body "{}"}))) => {:status :error})
       (fact "404" (github/wrap-http-call (fn [] (throw+ {:status 404 :request-time 0 :headers {} :body "{}"}))) => {:status :error :cause 404})
       (fact "Exception" (github/wrap-http-call (fn [] (throw (RuntimeException. "Some random failure")))) => (contains {:message "Some random failure" :status :error})))

(facts "about querying github api"
       (fact "reading a single repository"
             (github/get-project-repository "username" "repository") => (contains {:status :success})
             (provided (http/get "https://api.github.com/repos/username/repository") => {}))

       (fact "reading project repositories"
             (github/get-project-repositories "kuona") => (contains {:status :success :github []})
             (provided (http/get "https://api.github.com/users/kuona/repos") => {:body "[]"})))
