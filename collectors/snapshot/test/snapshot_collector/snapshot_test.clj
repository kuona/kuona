(ns snapshot-collector.snapshot-test
  (:require [midje.sweet :refer :all]
            [kuona-core.util :as util]
            [snapshot-collector.snapshot :refer :all]))

(facts "about data extraction"
       (fact (repository-id {}) => nil)
       (fact (repository-id {:id 1}) => 1))

(facts "extracting url"
       (fact (repository-git-url {}) => nil)
       (fact (repository-git-url {:url "/some/path"}) => "/some/path"))

(facts "url building"
       (fact (snapshot-url "base" 10) => "base/api/snapshots/10")
       (fact (snapshot-commits-url "base" 11) => "base/api/snapshots/11/commits"))

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

(facts "about line of code metrics"
       (fact (loc-metrics {}) => {:blank_lines          nil
                                  :code_lines           nil
                                  :comment_lines        nil
                                  :file_count           nil
                                  :blank_line_details   []
                                  :code_line_details    []
                                  :comment_line_details []
                                  :file_details         []})
       (fact "copied basic data"
             (loc-metrics {:metric {:activity {:blank-lines   1
                                               :code-lines    2
                                               :comment-lines 3
                                               :file-count    4}}}) => {:blank_lines          1
                                                                        :code_lines           2
                                                                        :comment_lines        3
                                                                        :file_count           4
                                                                        :blank_line_details   []
                                                                        :code_line_details    []
                                                                        :comment_line_details []
                                                                        :file_details         []})
       (fact "copied basic data"
             (loc-metrics {:metric {:activity {:blank-lines   1
                                               :code-lines    2
                                               :comment-lines 3
                                               :file-count    4
                                               :languages     [{:language      :foo
                                                                :file-count    1
                                                                :blank-lines   2
                                                                :comment-lines 3
                                                                :code-lines    4}
                                                               ]}}}) => {:blank_lines          1
                                                                         :code_lines           2
                                                                         :comment_lines        3
                                                                         :file_count           4
                                                                         :blank_line_details   [{:language :foo :count 2}]
                                                                         :code_line_details    [{:language :foo :count 4}]
                                                                         :comment_line_details [{:language :foo :count 3}]
                                                                         :file_details         [{:language :foo :count 1}]}))

