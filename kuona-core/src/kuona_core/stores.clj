(ns kuona-core.stores
  (:require [clojure.string :as string]
            [kuona-core.elasticsearch :as es]
            [slingshot.slingshot :refer :all]))

(def es-host (atom "http://localhost:9200"))

(defn es-index
  [index-name type]
  {:name (name index-name)
   :url  (string/join "/" [(deref es-host) (name index-name)])
   :type type})

(defn mapping-url [index]
  (string/join "/" [(-> index :url) "_mapping"]))

(defn count-url [index]
  (string/join "/" [(-> index :url) "_count"]))

(defn option-to-es-search-param [[k v]]
  (cond
    (= k :term) (str "q=" v)
    (= k :size) (str "size=" v)
    (= k :from) (str "from=" v)
    :else nil
    ))

(defn es-options [options]
  (select-keys options '(:term :size :from)))

(defn parse-integer
  [n]
  (cond
    (= (type n) java.lang.Long) n
    :else (try+ (. Integer parseInt n)
                (catch Object _ nil))))
(defn pagination-param
  [options]
  (let [page (-> options :page)
        size (-> options :size)
        page-number (parse-integer page)]
    (cond
      (and size (> page-number 1)) {:size size :from (* (- page-number 1) size)}
      (nil? size) {}
      (= page-number 1) {:size size}
      :else {})
    ))

(defn query-string [options]
  (let [pagination (pagination-param options)]
    (string/join "&" (filter #(not (nil? %)) (map option-to-es-search-param (merge options pagination))))))

(defn search-url
  ([index]
   (string/join "/" [(-> index :url) "_search"]))

  ([index options]
   (let [query-string (query-string options)
         path (string/join "/" [(-> index :url) "_search"])]
     (str path (if options (str "?" query-string))))))

(defn id-url [index id]
  (string/join "/" [(-> index :url) id]))

(defn update-url [index id]
  (string/join "/" [(-> index :url) id "_update"]))




;
;
;(def kuona-code
;  (es-index :kuona-code {:code {:properties {:id es/string-not-analyzed}}}))
