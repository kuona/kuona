(defproject test-service "0.0.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :main test-service.main
  :aot [test-service.main]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-jetty-adapter "1.5.0"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-midje "3.0.0"]]
  :ring {:handler test-service.handler/app}
  :profiles {:dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring/ring-mock "0.3.0"]
                                      [midje "1.9.0-alpha5"]]}
             :uberjar {:aot :all}})
