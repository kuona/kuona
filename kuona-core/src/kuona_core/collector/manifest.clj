(ns kuona-core.collector.manifest
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
  (let [participants (components manifest)
        preamble     (map participant-string participants)
        depends (dependencies manifest)
        plantuml-dependencies (plantuml-dependencies depends)
        elements     (flatten ["@startuml" preamble plantuml-dependencies "@enduml"])]
    (clojure.string/join "\n" elements))
  )
