(ns kuona-api.chat
  (:require [ring.util.response :refer [resource-response response status]]
            [kuona-core.store :as store]
            [kuona-core.stores :as stores]))

(defn message [msg]

  (response {:response
             {:message
              (let [text (-> msg :query)]
                (cond
                  (re-matches #"How many repos are there" text) (str "There are " (-> (store/get-count stores/repositories-store) :count) " repositories currently loaded")
                  :else "Sorry I did not understand"
                  ))
              }})
  )
