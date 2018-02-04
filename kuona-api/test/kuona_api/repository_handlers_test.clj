(ns kuona-api.repository-handlers-test
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [kuona-api.repository-handlers :as h]
            [kuona-api.test-helpers :as helper]
            [kuona-api.handler :refer :all]
            [kuona-core.github :as github]
            [midje.sweet :refer :all]))

(facts "about commit pagination"
       (fact (h/commits-page-link 1 11) => "/api/repositories/1/commits?page=11"))

(facts "about commit handling"
       (fact "returns error if commit does not have an id"
             (let [response (helper/mock-json-put app "/api/repositories/1/commits" {})]
               (:status response) => 400))
       (fact "returns commit if created"
             (:status (helper/mock-json-put app "/api/repositories/1/commits" {:id 234})) => 200
             (provided (http/put "http://localhost:9200/kuona-repositories/commits/234" {:headers {"content-type" "application/json; charset=UTF-8"},
                                                                                         :body    "{\"id\":234,\"repository_id\":\"1\"}"}) => {:status 200 :body (json/generate-string {:id 234})}))

       )

(facts "about testing repository links"
       (fact "projects with github source are tested with github"
             (let [project-request {:source     :github
                                    :username   "kuona"
                                    :repository "kuona-project"}]
               (h/test-project-url project-request) => (contains {:status 200 :body :result})
               (provided (github/get-project-repository "kuona" "kuona-project") => :result))))

(facts "about converting github data to repository record"
       (let [test-data {:project {:name              :test-name
                                  :description       :test-desc
                                  :owner             {
                                                      :avatar_url :test-avatar}

                                  :html_url          :test-html-url
                                  :created_at        :test-created-at
                                  :updated_at        :test-updated-at
                                  :open_issues_count :test-open-issues-count
                                  :watchers          :test-watchers
                                  :forks             :test-forks
                                  :size              :test-size
                                  }

                        }]
         (fact "copies the project name"
               (h/github-to-repository-record test-data) => (contains {:name :test-name}))
         (fact "copies the project description"
               (h/github-to-repository-record test-data) => (contains {:description :test-desc}))
         (fact "copies the project url"
               (h/github-to-repository-record test-data) => (contains {:project_url :test-html-url}))
         (fact "copies the created date"
               (h/github-to-repository-record test-data) => (contains {:created_at :test-created-at}))
         (fact "copies the updated date"
               (h/github-to-repository-record test-data) => (contains {:updated_at :test-updated-at}))
         (fact "copies the issues count"
               (h/github-to-repository-record test-data) => (contains {:open_issues_count :test-open-issues-count}))
         (fact "copies the watchers count"
               (h/github-to-repository-record test-data) => (contains {:watchers :test-watchers}))
         (fact "copies the fork count"
               (h/github-to-repository-record test-data) => (contains {:forks :test-forks}))
         (fact "copies the repo size"
               (h/github-to-repository-record test-data) => (contains {:size :test-size}))
         (fact "copies the original data into a github key"
               (h/github-to-repository-record test-data) => (contains {:github test-data}))))

