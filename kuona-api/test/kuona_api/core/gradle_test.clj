(ns kuona-api.core.gradle-test
  (:require [midje.sweet :refer :all]
            [kuona-api.core.gradle :as gradle])
  )


(facts "about gradle builds"
       (let [test-project-path "../test/gradle-dependencies"]
         (fact
           (:project (gradle/analyse-gradle-project test-project-path)) => (contains {:description nil}))
         (fact
           (gradle/analyse-gradle-project test-project-path)
           => {:project {:description nil
                         :jdk         "1.10"
                         :modules     [{:dependencies [{:exported false
                                                        :group    "org.antlr"
                                                        :name     "antlr4-runtime"
                                                        :scope    "RUNTIME"
                                                        :version  "4.7"}
                                                       {:exported false
                                                        :group    "junit"
                                                        :name     "junit"
                                                        :scope    "TEST"
                                                        :version  "4.12"}
                                                       {:exported false
                                                        :group    "org.hamcrest"
                                                        :name     "hamcrest-core"
                                                        :scope    "TEST"
                                                        :version  "1.3"}
                                                       {:exported false
                                                        :group    "org.antlr"
                                                        :name     "antlr4-runtime"
                                                        :scope    "TEST"
                                                        :version  "4.7"}]
                                        :description  nil
                                        :name         "gradle-dependencies"}]
                         :name        "gradle-dependencies"}})))
