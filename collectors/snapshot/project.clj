(defproject kuona-snapshot-collector "0.0.1"
  :description "Kuona snapshot collector. Collects point in time (HEAD) code and repository metrics"
  :url "http://example.com/FIXME"
  :license {:name "Apache V2.0"
            :url  "http://www.apache.org/licenses/"}
  :main snapshot-collector.core
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [org.slf4j/slf4j-log4j12 "1.7.25"]         ;; Required for clj-jgit
                 [clj-http "3.9.0"]
                 [cheshire "5.8.0"]
                 [clj-jgit "0.8.10"]
                 [com.jcabi/jcabi-log "0.18"]
                 [kuona-core "0.0.2"]]
  :plugins [[lein-midje "3.0.0"]
            [lein-ancient "0.6.14"]]
  :profiles {:dev     {:dependencies [[midje "1.9.1"]]}
             :uberjar {:aot :all}})
