(defproject kuona-http-collector "0.2.2"
  :description "Data collector for HTTP endpoints for Kuona project"
  :url "http://github.com/kuona/http-collector"
  :main http-collector.core
  :uberjar {:aot :all}
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [clj-http "3.1.0"]
                 [cheshire "5.6.3"]
                 [clojurewerkz/quartzite "2.0.0"]]
  :plugins [[lein-midje "3.0.0"]]
  :profiles {:dev     {:dependencies [[midje "1.9.0-alpha5"]]}
             :uberjar {:aot :all}})
