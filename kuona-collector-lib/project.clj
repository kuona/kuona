(defproject kuona-collector "0.0.1"
  :description "Kuona library to support collectors"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [clj-http "3.6.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.25"] ;; Required for clj-jgit
                 [clj-jgit "0.8.9"]
                 [cheshire "5.6.3"]
                 [slingshot "0.12.2"]
                 [clj-time "0.13.0"]
                 [org.clojure/data.xml "0.0.8"]]
  :plugins [[lein-midje "3.0.0"]]
  :profiles
  {:dev {:dependencies [[midje "1.9.0-alpha5"]
                        [com.jcabi/jcabi-log "0.17.1"]]}})

