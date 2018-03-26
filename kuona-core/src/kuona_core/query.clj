(ns kuona-core.query
  (:import [kuona.query Parser]
           (java.io ByteArrayInputStream)))

(defn parse
  "Wrapper for parser. Accepts a text representation of a query and returns an object representing the query"
  [text]
  (let [bytes (.getBytes text)
        s (ByteArrayInputStream. bytes)
        q (Parser/parse s)]
    {:terms (.getTerms q)
     }
    ))

(defn generate [text]
  (let [q (parse text)]
    {"simple_query_string" {"query" (reduce (fn [a b] (str a " " b)) (:terms q))}}))



