(ns kuona-core.elasticsearch)

(def string
  {:type "text"})
(def string-analyzed
  {:type  "keyword"
   :index "true"})

(def string-not-analyzed
  {:type "keyword" :index "false"})

(def timestamp
  {:type "date" :format "strict_date_optional_time||epoch_millis"})

(def long-integer
  {:type "long"})

(def disabled-object
  {:type "object" :enabled :false})

(def enabled-object
  {:type "object" :enabled :true})

