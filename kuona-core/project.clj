(defproject kuona-core "0.0.2"
  :description "Kuona core library"
  :url "http://example.com/FIXME"
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [clj-http "3.6.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.25"]
                 [clj-jgit "0.8.9"]
                 [cheshire "5.6.3"]
                 [slingshot "0.12.2"]
                 [clj-time "0.13.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [kuona/maven-dot-parser "0.1"]]
  :plugins [[lein-midje "3.0.0"]]
  :profiles
  {:dev {:dependencies [[midje "1.9.0-alpha5"]
                        [com.jcabi/jcabi-log "0.17.1"]]}})


