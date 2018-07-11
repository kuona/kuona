(ns kuona-core.collector.manifest
  (:require [clojure.java.io :as io])
  (:require [yaml.core :as yaml]
            [clojure.tools.logging :as log]))

(defn from-file [filename]
  (yaml/from-file filename))

(defn manifest-file
  [path]
  (cond
    (.exists (io/file path "manifest.yml")) (io/file path "manifest.yml")
    (.exists (io/file path "manifest.yaml")) (io/file path "manifest.yaml")
    (.exists (io/file path ".manifest.yml")) (io/file path ".manifest.yml")
    (.exists (io/file path ".manifest.yaml")) (io/file path ".manifest.yaml")
    :else nil))

(defn clean-manifest [m]
  {:manifest {:found true
              :description (-> m :manifest :description)
              :components  (into [] (map (fn [c]
                                           {:id           (-> c :id)
                                            :description  (-> c :description)
                                            :path         (-> c :path)
                                            :dependencies (into [] (map (fn [d]
                                                                          (merge {:id nil :kind "component"} d)) (-> c :dependencies)))
                                            }) (-> m :manifest :components)))}})

(defn collect
  "Generates a dependency diagram for the repository. If a manifest.yml or .manifest.yml
   file is found in the localpath then it is used to generate the diagram otherwise a default diagram is written"
  [localpath]

  (let [manifest-file (manifest-file localpath)]
    (cond
      manifest-file (let [text (slurp manifest-file)]
                      (clean-manifest (yaml/parse-string text)))
      :else {:manifest {:found false}})))
