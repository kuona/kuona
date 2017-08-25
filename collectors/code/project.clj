(defproject kuona-code-collector "0.1.0-SNAPSHOT"
  :description "Code metric collector for Kuona"
  :url "http://example.com/FIXME"
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :main code-collector.core
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
                 [org.clojure/data.xml "0.0.8"]
                 [kuona-core "0.0.2"]]
  :plugins [[lein-midje "3.0.0"]]
  :profiles {:dev     {:dependencies [[midje "1.9.0-alpha5"]]}
             :uberjar {:aot :all}})
