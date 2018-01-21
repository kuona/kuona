(defproject kuona-git-collector "0.2.2"
  :description "Kuona Git collector"
  :url "https://github.com/kuona/git-collector"
  :main git-collector.core
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [org.slf4j/slf4j-log4j12 "1.7.25"] ;; Required for clj-jgit
                 [com.jcabi/jcabi-log "0.18"]
                 [clj-http "3.7.0"]
                 [cheshire "5.8.0"]
                 [clj-jgit "0.8.10"]
                 [kuona-core "0.0.2"]
                 [org.clojure/tools.cli "0.3.5"]]
  :plugins [[lein-midje "3.0.0"]
            [lein-ancient "0.6.14"]]
  :profiles {:dev     {:dependencies [[midje "1.9.1"]]}
             :uberjar {:aot :all}})
