(ns tfs-crawler.crawler-test
  (:require [midje.sweet :refer :all]
            [cheshire.core :refer :all]
            [tfs-crawler.crawler :as crawler]
            [clj-http.client :as http]))

(facts "about visual studio API urls"
       (fact "contains the organisation name"
             (crawler/vs-url "foo") => "https://foo.visualstudio.com/_apis/git/repositories?api-version=4.1"))


(facts "about repository data conversation"
       (fact
         (crawler/tfs-to-repository-entry {:sshUrl "foo"}) => {:github_language nil
                                                               :last_analysed   nil
                                                               :project         {:sshUrl "foo"}
                                                               :source          :tfs
                                                               :url             "foo"}))

(facts "about reading repositories"
       (fact
         (crawler/read-available-repositories "http://vs" "token") => {:count 0}
         (provided (http/get "http://vs" {:basic-auth ["" "token"]}) => {:body (generate-string {:count 0})})))
