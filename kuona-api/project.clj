(defproject kuona-api "0.0.2"
  :description "Kuona HTTP API service"
  :url "https://github.com/kuona/kuona"
  :min-lein-version "2.0.0"
  :main kuona-api.main
  :aot [kuona-api.main]
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :repositories [["gradle" "https://repo.gradle.org/gradle/libs-releases-local/"]]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.cli "0.4.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [cheshire "5.8.1"]
                 [clj-http "3.9.1"]
                 [clj-jgit "0.8.10"]
                 [clj-time "0.15.1"]
                 [clojurewerkz/quartzite "2.1.0"]
                 [com.jcabi/jcabi-log "0.18"]
                 [commons-codec/commons-codec "1.11"]
                 [compojure "1.6.1"]
                 [io.forward/yaml "1.0.9"]
                 [kuona/parsers "0.2"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.2"]
                 [org.gradle/gradle-core "5.0"]
                 [org.gradle/gradle-tooling-api "5.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.25"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [ring/ring-json "0.4.0"]
                 [slingshot "0.12.2"]
                 ]
  :plugins [[lein-ring "0.9.7"]
            [lein-midje "3.0.0"]
            [lein-ancient "0.6.14"]
            [lein-pprint "1.2.0"]
            [lein-asciidoctor "0.1.16"]]
  :asciidoctor {:sources "doc/*.adoc"
                :to-dir  "resources/doc"}
  :ring {:handler      kuona-api.handler/app
         :auto-reload? true
         :auto-refresh true}
  :profiles {:dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring/ring-mock "0.3.2"]
                                      [midje "1.9.4"]]}
             :uberjar {:aot :all}})
