(ns kuona-api.main
  (:require [slingshot.slingshot :refer [try+]]
            [ring.adapter.jetty :as jetty]
            [clojure.tools.logging :as log]
            [kuona-api.core.scheduler :as scheduler]
            [kuona-api.handler :as service]
            [kuona-api.core.stores :as stores]
            [kuona-api.core.cli :as kcli]
            [kuona-api.core.workspace :refer [set-workspace-path]])
  (:gen-class))

(def cli-options
  [["-c" "--config FILE" "API Key used to communicate with the API service (deprecated/ignored)" :default "kuona-properties.json"]
   ["-w" "--workspace WORKSPACE" "Workspace for working files and repositories"]
   ["-s" "--store STORE" "Prefix for elastic search data store index names" :default "kuona"]
   ["-r" "--rebuild" "Rebuild/Destroy the current data stores" :default false]
   ["-m" "--mode MODE" "Run mode standalone/passive. Standalone a local scheduler runs to collect data. In passive mode external collection is assumed" :default "standalone"]
   ["-p" "--port PORT" "API port" :default 8080]
   ["-h" "--help"]])



(defn start-application
  [port mode]
  (log/info "Starting API on port " port)
  (try+
    (stores/create-stores)
    (if (= mode "standalone")
      (scheduler/start))
    (kcli/exit 0 (jetty/run-jetty #'service/app {:port port}))
    (catch [:type :config/missing-parameter] {:keys [parameter p]}
      (log/error "Missing configuration parameter " p))))

(defn -main
  [& args]
  (let [options (kcli/configure "Kuona server" cli-options args)]
    (set-workspace-path (-> options :workspace))
    (stores/set-store-prefix (-> options :store))
    (if (-> options :rebuild)
      (do
        (println "Are you sure you want to delete all the stores? This step is irreversible: (y/N) ")
        (let [user-input (read-line)]
          (if (or (= user-input "Y") (= user-input "y"))
            (stores/rebuild)
            (kcli/exit 1 "Cancelling - indexes not updated")))))
    (if (not (kuona-api.core.workspace/workspace-path-valid?)) (kcli/exit 1 "Invalid workspace path"))
    (log/info "Using workspace" (-> options :workspace))
    (start-application (-> options :port) (-> options :mode))))

