(ns environment-service.util)

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn timestamp
  []
  (new java.util.Date))

