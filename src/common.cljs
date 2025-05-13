(ns common)

(def ^:export app-container
  (.querySelector js/document "#app"))

(def ^:export canvas
  (.querySelector js/document "#canvas"))

(def ^:export hud
  (.querySelector js/document "#hud"))

(def ^:export physics-to-mesh-scaling-factor 100)
