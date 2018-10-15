(ns kuona-api.core.integration.search-code-server
  (:require [kuona-api.core.http :as http]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer :all])
  (:import (org.apache.commons.codec.digest HmacUtils))
  (:gen-class))

(defn list-repositories
  [key url]
  (try+
    (if (and (-> key :public) (-> key :private))
      (let [pub         (str "pub=" (:public key))
            result      (org.apache.commons.codec.digest.HmacUtils/hmacSha1Hex (-> key :private) pub)
            request-url (str url "/api/repo/list/?sig=" result "&" pub)]
        (http/json-get request-url))
      (http/json-get (str url "/api/repo/list/")))
    (catch [:status 400] {:keys [request-time headers body]}
      {:status  :error
       :message "Bad Request"})
    (catch [:status 404] {:keys [request-time headers body]}
      {:status "Not found"
       :cause  404})
    (catch Object _
      (log/error "Unexpected exception " (:message &throw-context))
      {:status  :error
       :message (:message &throw-context)
       :cause   (:cause &throw-context)})))
