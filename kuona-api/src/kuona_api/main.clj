(ns kuona-api.main
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [slingshot.slingshot :refer [try+]]
            [ring.adapter.jetty :as jetty]
            [clojure.tools.logging :as log]
            [kuona-core.scheduler :as scheduler]
            [kuona-api.handler :as service]
            [kuona-core.stores :as stores]
            [kuona-core.cli :as kcli])
  (:gen-class))

(def cli-options
  [["-c" "--config FILE" "API Key used to communicate with the API service (deprecated/ignored)" :default "kuona-properties.json"]
   ["-w" "--workspace WORKSPACE" "Workspace for working files and repositories"]
   ["-p" "--port PORT" "API port" :default 9001]
   ["-h" "--help"]])



(defn start-application
  [port]
  (log/info "Starting API on port " port)
  (try+
    (stores/create-stores)
    (scheduler/start)
    (kcli/exit 0 (jetty/run-jetty #'service/app {:port port}))
    (catch [:type :config/missing-parameter] {:keys [parameter p]}
      (log/error "Missing configuration parameter " p))))

(defn -main
  [& args]
  (let [options (kcli/configure "Kuona server" cli-options args)]
    (kuona-core.workspace/set-workspace-path (-> options :workspace))
    (if (not (kuona-core.workspace/workspace-path-valid?)) (kcli/exit 1 "Invalid workspace path"))
    (log/info "Using workspace" (-> options :workspace))
    (start-application (-> options :port))))

