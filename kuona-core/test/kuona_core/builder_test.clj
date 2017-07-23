(ns kuona-core.builder-test
  (:require [midje.sweet :refer :all]
            [kuona-core.builder :refer :all]
            [kuona-core.maven :as maven]
            [clojure.data.xml :as xml]))

(facts "about build tool matching"
       (fact "handles nil"
             ((build-tool nil) nil) => {})
       (fact "handles simple filename"
             ((build-tool "makefile") "makefile") => {:builder "Make" :path "makefile"})
       (fact "handles path"
             ((build-tool "/some/path/makefile") "/some/path/makefile") => {:builder "Make" :path "/some/path/makefile"})
       (fact "handles rakefiles"
             ((build-tool "/some/path/Rakefile") "/some/path/Rakefile") => {:builder "Rake" :path "/some/path/Rakefile"})
       (fact "handles maven"
             ((build-tool "/some/path/pom.xml") "/some/path/pom.xml") => {:artifact {:artifactId "kuona-dashboard"
                                                                                     :groupId    "kuona"
                                                                                     :name       "Kuona analytics for software development teams"
                                                                                     :version    "0.1"}
                                                                          :builder  "Maven" :path "/some/path/pom.xml"}
             (provided (maven/load-pom-file "/some/path/pom.xml") => (xml/parse-str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">
    <modelVersion>4.0.0</modelVersion>
    <groupId>kuona</groupId>
    <artifactId>kuona-dashboard</artifactId>
    <packaging>pom</packaging>
    <name>Kuona analytics for software development teams</name>
    <version>0.1</version>
</project>"))
             )
       (fact "recognises leiningen"
             ((build-tool "/fo/project.clj") "/fo/project.clj") => {:builder "leiningen" :path "/fo/project.clj"}) )
