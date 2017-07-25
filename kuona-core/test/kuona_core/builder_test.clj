(ns kuona-core.builder-test
  (:require [midje.sweet :refer :all]
            [kuona-core.builder :refer :all]
            [kuona-core.maven :as maven]
            [clojure.data.xml :as xml]
            [kuona-core.util :refer :all]))

(facts "about build tool matching"
       (fact "handles nil"
             ((build-tool nil nil) nil) => nil)
       (fact "handles simple filename"
             ((build-tool "makefile" "makefile") "makefile") => {:builder "Make" :path "makefile"})
       (fact "handles path"
             ((build-tool "/some/path/makefile" "makefile") "/some/path/makefile") => {:builder "Make" :path "makefile"})
       (fact "handles rakefiles"
             ((build-tool "/some/path/Rakefile" "Rakefile") "/some/path/Rakefile") => {:builder "Rake" :path "Rakefile"})
       (fact "handles maven and calls analyser"
             ((build-tool "/some/path/pom.xml" "pom.xml") "/some/path/pom.xml") => {:artifact {:artifactId "kuona-dashboard"
                                                                                               :groupId    "kuona"
                                                                                               :name       "Kuona analytics for software development teams"
                                                                                               :version    "0.1"}
                                                                                    :builder  "Maven" :path "pom.xml"}

             
             (provided (maven/analyse-pom-file "/some/path/pom.xml") => {:artifact {:artifactId "kuona-dashboard"
                                                                                    :groupId    "kuona"
                                                                                    :name       "Kuona analytics for software development teams"
                                                                                    :version    "0.1"}}))
       (fact "recognises leiningen"
             ((build-tool "/fo/project.clj" "/fo/project.clj") "/fo/project.clj") => {:builder "Leiningen" :path "/fo/project.clj"}) )


(facts "about project scanning"
       (collect-builder-metrics ".") =>  [{:artifact {:artifactId    "kuona-core"
                                                      :groupId       "kuona-core"
                                                      :name          "kuona-core"
                                                      :version       "0.0.2"
                                                      :description   "Kuona core library"
                                                      :inceptionYear nil
                                                      :packaging     "jar"
                                                      :url           "http://example.com/FIXME"}
                                           :builder  "Maven"
                                           :path     "/pom.xml"}
                                          {:builder "Leiningen"
                                           :path    "/project.clj"}
                                          {:artifact {:artifactId    "kuona-dashboard"
                                                      :groupId       "kuona"
                                                      :name          "Kuona analytics for software development teams"
                                                      :version       "0.1"
                                                      :description   nil
                                                      :inceptionYear nil
                                                      :packaging     "pom"
                                                      :url           nil}
                                           :builder  "Maven"
                                           :path     "/test/pom.xml"}])
