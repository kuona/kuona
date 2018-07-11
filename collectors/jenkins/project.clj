(defproject kuona-jenkins-collector "0.0.1"
  :description "Jenkins build metric collector"
  :url "http://github.com/kuona"
  :main jenkins-collector.main
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [clj-http "3.9.0"]
                 [org.clojure/data.zip "0.1.2"]
                 [cheshire "5.8.0"]
                 [slingshot "0.12.2"]
                 [com.jcabi/jcabi-log "0.18"]
                 [kuona-core "0.0.2"]]
  :plugins [[lein-midje "3.0.0"]
            [lein-ancient "0.6.14"]]
  :profiles {:dev     {:dependencies [[midje "1.9.1"]]}
             :uberjar {:aot :all}})
