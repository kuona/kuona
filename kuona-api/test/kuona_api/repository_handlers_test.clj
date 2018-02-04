(ns kuona-api.repository-handlers-test
  (:require [cheshire.core :refer :all]
            [clj-http.client :as http]
            [kuona-api.environments :refer :all]
            [kuona-api.repository-handlers :as h]
            [kuona-api.test-helpers :as helper]
            [kuona-api.handler :refer :all]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(facts "about commit pagination"
       (fact (h/commits-page-link 1 11) => "/api/repositories/1/commits?page=11"))

(facts "about commit handling"
       (fact "returns error if commit does not have an id"
             (let [response (helper/mock-json-put app "/api/repositories/1/commits" {})]
               (:status response) => 400))
       (fact "returns commit if created"
             (:status (helper/mock-json-put app "/api/repositories/1/commits" {:id 234})) => 200
             (provided (http/put "http://localhost:9200/kuona-repositories/commits/234" {:headers {"content-type" "application/json; charset=UTF-8"},
                                                                                         :body    "{\"id\":234,\"repository_id\":\"1\"}"}) => {:status 200 :body (generate-string {:id 234})}))

       )

(facts "about testing repository links"
       (fact "projects with github source are tested with github"
             (let [project-request {:source :github}]
               (h/test-project-url project-request) => :true
               (provided (h/test-github-project project-request) => :true)))

       (fact "Request project if user and project provided"
             (let [project-request {:source :github
                                    :url    "https://github.com/kuona/kuona-project"}]
               (h/test-github-project project-request) => "YES"
               (provided (http/get "https://api.github.com/repos/kuona/kuona-project") => {:status 200 :body ""})
               )
             )
       )
