(ns kuona-core.query
  (:import [kuona.query Parser]
           (java.io ByteArrayInputStream)
           (java.nio.charset StandardCharsets)))

(defn parse
  "Wrapper for parser. Accepts a text representation of a query and returns an object representing the query"
  [text]
  (let [bytes (.getBytes text)
        s (ByteArrayInputStream. bytes)
        q (Parser/parse s)]
    {:terms (.getTerms q)
     }
    ))



