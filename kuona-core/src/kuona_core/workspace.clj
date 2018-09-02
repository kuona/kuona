(ns kuona-core.workspace
  (:require [kuona-core.util :as util]))

(def workspace-path (atom nil))

(defn reset-workspace-path! []
  (reset! workspace-path nil))

(defn get-workspace-path []
  (deref workspace-path))

(defn set-workspace-path [path]
  (reset! workspace-path path))

(defn workspace-path-valid? []
  (cond
    (nil? (get-workspace-path)) false
    :else (util/directory? (get-workspace-path))))
