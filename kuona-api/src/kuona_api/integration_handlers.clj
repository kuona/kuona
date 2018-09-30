(ns kuona-api.integration-handlers
  (:require [clojure.tools.logging :as log]
            [ring.util.response :refer [resource-response response status]]
            [kuona-core.integration.search-code-server :as search-code-server]))

(defn test-integration [config]
  (log/info "Testing integration" config)
  (response (cond
              (= (-> config :integration :type) "searchcode") (search-code-server/list-repositories (-> config :integration :api_key) (-> config :integration :url))
              :else {:error {:description "Unrecognised integration type"}})))
