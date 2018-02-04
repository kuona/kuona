(ns kuona-core.github
  (:require [clj-http.client :as http]
            [slingshot.slingshot :refer :all]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [kuona-core.util :as util]))


(defn get-project-repository
  [username repository]

  (try+
    (let [url (string/join "/" ["https://api.github.com/repos" username repository])]
      {:status :success
       :github (util/parse-json-body (http/get url))
       })
    (catch [:status 400] {:keys [request-time headers body]}
      (let [error (util/parse-json body)]
        (log/info "Bad request" error)
        {:status :error}))
    (catch [:status 404] {:keys [request-time headers body]}
      (let [error (util/parse-json body)]
        (log/info "Bad request" error)
        {:status :error
         :cause 404}))
    (catch Object _
      (log/error (:throwable &throw-context) "Unexpected error reading schema" username repository)
      {:status :error})))
