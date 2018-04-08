(ns tfs-crawler.crawler-test
  (:require [midje.sweet :refer :all]
            [cheshire.core :refer :all]
            [tfs-crawler.crawler :as crawler]
            [clj-http.client :as http]))

(facts "about visual studio API urls"
       (fact "contains the organisation name"
             (crawler/vs-url "foo") => "https://foo.visualstudio.com/_apis/git/repositories?api-version=4.1"))


(facts "about repository data conversation"
       (fact "sets source to tfs"
             (crawler/tfs-to-repository-entry {}) => (contains {:source :tfs}))
       (fact "sets last analysed field to nil"
             (crawler/tfs-to-repository-entry {}) => (contains {:last_analysed nil}))
       (fact "sets the url to the sshUrl from the project"
             (crawler/tfs-to-repository-entry {:sshUrl "foo"}) => (contains {:url "foo"}))
       (fact "copies project name"
             (crawler/tfs-to-repository-entry {:name "foo-name"}) => (contains {:name "foo-name"}))
       (fact "copies the source into the project field"
             (let [input {:random :value}]
               (crawler/tfs-to-repository-entry input) => (contains {:project input}))))

(facts "about reading repositories"
       (fact
         (crawler/read-available-repositories "http://vs" "token") => {:count 0}
         (provided (http/get "http://vs" {:basic-auth ["" "token"]}) => {:body (generate-string {:count 0})})))
