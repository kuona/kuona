(defproject kuona-api "0.0.1"
  :description "Kuona HTTP API service"
  :url "https://github.com/kuona/kuona-project"
  :min-lein-version "2.0.0"
  :main kuona-api.main
  :aot [kuona-api.main]
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.3.7"]
                 [org.clojure/tools.logging "0.4.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [compojure "1.6.1"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]
                 [cheshire "5.8.0"]
                 [slingshot "0.12.2"]
                 [clojurewerkz/quartzite "2.1.0"]
                 [io.forward/yaml "1.0.9"]
                 [com.jcabi/jcabi-log "0.18"]
                 [kuona-core "0.0.2"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-midje "3.0.0"]
            [lein-ancient "0.6.14"]
            [lein-pprint "1.2.0"]]
  :ring {:handler      kuona-api.handler/app
         :auto-reload? true
         :auto-refresh true}
  :profiles {:dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring/ring-mock "0.3.2"]
                                      [midje "1.9.2"]]}
             :uberjar {:aot :all}})
