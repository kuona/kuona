(defproject kuona-core "0.0.2"
  :description "Kuona core library"
  :url "https://github.com/kuona/kuona-project"
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :repositories [["gradle" "https://repo.gradle.org/gradle/libs-releases-local/"]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [org.clojure/data.zip "0.1.2"]
                 [clj-http "3.9.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.25"]
                 [clj-jgit "0.8.10"]
                 [cheshire "5.8.1"]
                 [slingshot "0.12.2"]
                 [clj-time "0.14.4"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/tools.cli "0.4.1"]
                 [clojurewerkz/quartzite "2.1.0"]
                 [io.forward/yaml "1.0.9"]
                 [com.jcabi/jcabi-log "0.18"]
                 [kuona/parsers "0.2"]
                 [commons-codec/commons-codec "1.11"]
                 [org.gradle/gradle-core "4.10.2"]
                 [org.gradle/gradle-tooling-api "4.10.2"]]
  :plugins [[lein-midje "3.0.0"]
            [lein-ancient "0.6.14"]
            [lein-asciidoctor "0.1.16"]]
  :asciidoctor {:sources          "doc/*.adoc"
                :source-highlight true
                :to-dir           "target/doc"}
  :profiles
  {:dev {:dependencies [[midje "1.9.3"]]}})


