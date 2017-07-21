(ns code-collector.core
  (:require [clojure.java.shell :as shell]
            [clojure.data.xml :as xml]
            [cheshire.core :refer :all]
            [clojure.set :only [rename-keys]]
            [clojure.pprint :only [pprint]]
            [kuona-collector.util :refer :all])
  (:gen-class))

(defn -main
  [& args]
  (clojure.pprint/pprint  (add-metadata (map cloc-language-metric (remove-cloc-header (cloc "."))))))


