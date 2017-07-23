(ns kuona-api.mapping)

(def es-string-type
  { :type "string" })

(def es-string-not-analyzed-type
  { :type "string"  :index "not_analyzed"   })

(def es-timestamp-type
  { :type "date" :format "strict_date_optional_time||epoch_millis" })

(def comment-type
  {:entry es-string-type
   :timestamp es-timestamp-type
   :useability es-string-not-analyzed-type
   :user es-string-not-analyzed-type })

(def comment-mapping-type
  {:comments {:properties comment-type}})

(def environment-mapping-type
  {:environments {:properties {:name es-string-not-analyzed-type
                               :version es-string-not-analyzed-type
                               :status es-string-not-analyzed-type
                               :tags es-string-not-analyzed-type
                               :comment {:properties comment-type}}}})
