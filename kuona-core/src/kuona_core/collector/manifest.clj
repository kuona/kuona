(ns kuona-core.collector.manifest
  (:require [kuona-core.plantuml :as plantuml])
  (:require [clojure.java.io :as io])
  (:require [yaml.core :as yaml]))

(defn from-file [filename]
  (yaml/from-file filename))

(defn component-to-participant [c]
  {:name (-> c :id)
   :kind (or (-> c :kind) "component")}
  )

(defn component-dependencies-to-participant
  [d]
  (map #(-> %
            component-to-participant) (-> d :dependencies))
  )

(defn components [manifest]
  (set (flatten (concat (map component-to-participant (-> manifest :manifest :components))
                        (map component-dependencies-to-participant (-> manifest :manifest :components)))))
  )

(defn participant-string [component]
  (str (-> component :kind) " " (-> component :name)))

(defn dependencies [manifest]
  (flatten (map (fn [c]
                  (map (fn [d]
                         {:from (-> c :id) :to (-> d :id)}) (-> c :dependencies))
                  ) (-> manifest :manifest :components)))

  )

(defn plantuml-dependencies
  [pairs]
  (map (fn [p] (str (:from p) " --> " (:to p))) pairs))

(defn manifest-uml [manifest]
  (let [participants          (components manifest)
        preamble              (map participant-string participants)
        depends               (dependencies manifest)
        plantuml-dependencies (plantuml-dependencies depends)
        elements              (flatten ["@startuml" preamble plantuml-dependencies "@enduml"])]
    (clojure.string/join "\n" elements))
  )

(defn manifest-file
  [path]
  (cond
    (.exists (io/file path "manifest.yml")) (io/file path "manifest.yml")
    (.exists (io/file path "manifest.yaml")) (io/file path "manifest.yaml")
    (.exists (io/file path ".manifest.yml")) (io/file path ".manifest.yml")
    (.exists (io/file path ".manifest.yaml")) (io/file path ".manifest.yaml")
    :else nil))

(def default-diagram
  "@startuml
[No Manifest Found]
@enduml")

(defn collect
  "Generates a dependency diagram for the repository. If a manifest.yml or .manifest.yml
   file is found in the localpath then it is used to generate the diagram otherwise a default diagram is written"
  [repository-id localpath workspace-path]

  (let [manifest-file (manifest-file localpath)
        image-path    (str workspace-path "/" repository-id ".svg")]
    (cond
      manifest-file (let [text (slurp manifest-file)
                          y    (yaml/parse-string text)
                          uml  (manifest-uml y)]
                      (plantuml/generate-image uml image-path))
      :else (plantuml/generate-image default-diagram image-path))))
