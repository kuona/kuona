(ns kuona-api.core.collector.snapshot-test
  (:require [midje.sweet :refer :all]
            [kuona-api.core.collector.snapshot :refer :all]))

(facts "lines of code counts"
       (fact (language-x-count {:language   :foo
                                :line-count 123} :line-count) => {:count    123
                                                                  :language :foo}))

(facts "about project metrics"
       (fact (project-metrics {}) => {:description       nil
                                      :forks_count       nil
                                      :language          nil
                                      :name              nil
                                      :open_issues_count nil
                                      :owner_avatar_url  nil
                                      :pushed_at         nil
                                      :size              nil
                                      :stargazers_count  nil
                                      :updated_at        nil
                                      :watchers_count    nil})
       (fact (project-metrics {:description       "description"
                               :forks_count       "forks"
                               :language          "lang"
                               :name              "name"
                               :open_issues_count "issues"
                               :owner             {:avatar_url "avatar"}
                               :pushed_at         "pushed"
                               :size              "size"
                               :stargazers_count  "stargazers"
                               :updated_at        "updated"
                               :watchers_count    "watcher"}) => {:description       "description"
                                                                  :forks_count       "forks"
                                                                  :language          "lang"
                                                                  :name              "name"
                                                                  :open_issues_count "issues"
                                                                  :owner_avatar_url  "avatar"
                                                                  :pushed_at         "pushed"
                                                                  :size              "size"
                                                                  :stargazers_count  "stargazers"
                                                                  :updated_at        "updated"
                                                                  :watchers_count    "watcher"})
       (fact (project-metrics {:extra_param 1}) => {:description       nil
                                                    :forks_count       nil
                                                    :language          nil
                                                    :name              nil
                                                    :open_issues_count nil
                                                    :owner_avatar_url  nil
                                                    :pushed_at         nil
                                                    :size              nil
                                                    :stargazers_count  nil
                                                    :updated_at        nil
                                                    :watchers_count    nil}))

(facts "about snapshot structure"
       (fact (create-snapshot {:name "project"} :loc-data :builder) => {:build      :builder
                                                                        :content    :loc-data
                                                                        :repository {:description       nil
                                                                                     :forks_count       nil
                                                                                     :language          nil
                                                                                     :name              "project"
                                                                                     :open_issues_count nil
                                                                                     :owner_avatar_url  nil
                                                                                     :pushed_at         nil
                                                                                     :size              nil
                                                                                     :stargazers_count  nil
                                                                                     :updated_at        nil
                                                                                     :watchers_count    nil}}))

(facts "about line of code metrics in snapshots"
       (fact (loc-metrics {}) => (throws AssertionError))
       (fact "copied basic data"
             (loc-metrics {:metric {:activity {:blanks   1
                                               :code     2
                                               :comments 3
                                               :files    4}}}) => {:blank_lines          1
                                                                   :code_lines           2
                                                                   :comment_lines        3
                                                                   :file_count           4
                                                                   :blank_line_details   []
                                                                   :code_line_details    []
                                                                   :comment_line_details []
                                                                   :file_details         []})
       (fact "copied basic data"
             (loc-metrics {:metric {:activity {:blanks    1
                                               :code      2
                                               :comments  3
                                               :files     4
                                               :languages [{:language :foo
                                                            :files    1
                                                            :blanks   2
                                                            :comments 3
                                                            :code     4}
                                                           ]}}}) => {:blank_lines          1
                                                                     :code_lines           2
                                                                     :comment_lines        3
                                                                     :file_count           4
                                                                     :blank_line_details   [{:language :foo :count 2}]
                                                                     :code_line_details    [{:language :foo :count 4}]
                                                                     :comment_line_details [{:language :foo :count 3}]
                                                                     :file_details         [{:language :foo :count 1}]}))

(facts "about json requests"
       (fact "content type is application/json"
             (:headers (kuona-api.core.http/build-json-request {})) => {"content-type" "application/json; charset=UTF-8"})
       (fact "handles empty json object"
             (:body (kuona-api.core.http/build-json-request {})) => "{}")
       (fact "converts hashmap to json text"
             (:body (kuona-api.core.http/build-json-request {:key :value})) => "{\"key\":\"value\"}"))

