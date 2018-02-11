(ns code-collector.core
  (:require [cheshire.core :refer :all]
            [clojure.set :only [rename-keys]]
            [clojure.pprint :only [pprint]]
            [kuona-core.cloc :as cloc]
            [kuona-core.util :refer :all])
  (:gen-class))

(defn -main
  [& args]
  (clojure.pprint/pprint  (cloc/add-metadata (map cloc/cloc-language-metric (cloc/remove-cloc-header (cloc/cloc "."))))))


