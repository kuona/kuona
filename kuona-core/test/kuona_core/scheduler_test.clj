(ns kuona-core.scheduler-test
  (:require [midje.sweet :refer :all]
            [kuona-core.scheduler :as scheduler]))

(facts "About collector configuration checks"
       (fact (scheduler/github-org-collector-config? {}) => false)
       (fact (scheduler/tfs-org-collector-config? {}) => false)
       (fact (scheduler/github-org-collector-config? {:collector_type "VCS" :collector "GitHubOrg"}) => true)
       (fact (scheduler/tfs-org-collector-config? {:collector_type "VCS" :collector "TFS"}) => true))
