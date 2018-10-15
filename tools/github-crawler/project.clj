(defproject github-crawler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/kuona/kuona-project"
  :main github-crawler.core
  :license {:name "Apache V2.0"
            :url  "http://www.apache.org/licenses/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [com.jcabi/jcabi-log "0.17.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.25"]
                 [clj-http "3.1.0"]
                 [clj-jgit "0.8.9"]
                 [cheshire "5.6.3"]
                 [kuona-api "0.0.2"]
                 [org.clojure/tools.cli "0.3.5"]]
  :plugins [[lein-midje "3.2.1"]
            [lein-ancient "0.6.14"]]
  :profiles {:dev {:dependencies [[midje "1.9.1"]]}
             :uberjar {:aot :all}})
