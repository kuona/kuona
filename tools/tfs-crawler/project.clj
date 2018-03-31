(defproject tfs-crawler "0.1.0"
  :description "TFS account crawler - reads project repositories."
  :main tfs-crawler.core
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [com.jcabi/jcabi-log "0.18"]
                 [clj-http "3.8.0"]
                 [cheshire "5.8.0"]
                 [kuona-core "0.0.2"]
                 [org.clojure/tools.cli "0.3.5"]]
  :plugins [[lein-midje "3.2.1"]
            [lein-ancient "0.6.14"]]
  :profiles {:dev     {:dependencies [[midje "1.9.1"]]}
             :uberjar {:aot :all}})
