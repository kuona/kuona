(defproject kuona-core "0.0.2"
  :description "Kuona core library"
  :url "https://github.com/kuona/kuona-project"
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [clj-http "3.9.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.25"]
                 [clj-jgit "0.8.10"]
                 [cheshire "5.8.0"]
                 [slingshot "0.12.2"]
                 [clj-time "0.14.3"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/tools.cli "0.3.7"]
                 [clojurewerkz/quartzite "2.1.0"]
                 [kuona/maven-dot-parser "0.1"]
                 [kuona/query-parser "0.0.1"]
                 [io.forward/yaml "1.0.8"]
                 [com.jcabi/jcabi-log "0.18"]]
  :plugins [[lein-midje "3.0.0"]
            [lein-ancient "0.6.14"]]
  :profiles
  {:dev {:dependencies [[midje "1.9.1"]]}})


