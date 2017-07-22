(defproject jenkins-collector "0.0.1"
  :description "Jenkins build metric collector"
  :url "http://github.com/kuona"
  :main jenkins-collector.main
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [clj-http "3.6.0"]
                 [cheshire "5.6.3"]
                 [org.clojure/tools.cli "0.3.5"]
                 [slingshot "0.12.2"]
                 [kuona-core "0.0.2"]]
  :plugins [[lein-midje "3.0.0"]]
  :profiles {:dev     {:dependencies [[midje "1.9.0-alpha5"]
                                      [com.jcabi/jcabi-log "0.17.1"]]}
             :uberjar {:aot :all}})
