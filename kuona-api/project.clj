(defproject kuona-api "0.0.2"
  :description "Kuona HTTP API service"
  :url "https://github.com/kuona/kuona"
  :min-lein-version "2.0.0"
  :main kuona-api.main
  :aot [kuona-api.main]
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.4.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [compojure "1.6.1"]
                 [ring/ring-jetty-adapter "1.7.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]
                 [cheshire "5.8.1"]
                 [slingshot "0.12.2"]
                 [clojurewerkz/quartzite "2.1.0"]
                 [io.forward/yaml "1.0.9"]
                 [com.jcabi/jcabi-log "0.18"]
                 [commons-codec/commons-codec "1.11"]
                 [buddy/buddy-auth "2.1.0"]
                 [tuddman/neocons "3.2.1-SNAPSHOT"]
                 [kuona-core "0.0.2"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-midje "3.0.0"]
            [lein-ancient "0.6.14"]
            [lein-pprint "1.2.0"]
            [lein-asciidoctor "0.1.16"]]
  :asciidoctor {:sources "doc/*.adoc"
                :to-dir  "resources/doc"}
  :ring {:handler      kuona-api.handler/app
         :auto-reload? true
         :auto-refresh true}
  :profiles {:dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring/ring-mock "0.3.2"]
                                      [midje "1.9.3"]]}
             :uberjar {:aot :all}})
