(defproject environment-service "0.0.1"
  :description "Kuona environments service"
  :url "https://github.com/kuona/environment-service"
  :min-lein-version "2.0.0"
  :main environment-service.main
  :aot [environment-service.main]
  :uberjar { :aot :all }
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [compojure "1.5.1"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [cheshire "5.6.3"]
                 [clojurewerkz/elastisch "3.0.0-beta2"]
                 [slingshot "0.12.2"]
                 [kuona-collector "0.0.1"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-midje "3.0.0"]]
  :ring {:handler environment-service.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]
                        [midje "1.9.0-alpha5"]]}})
