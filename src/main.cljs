(ns main
  (:require ["three" :as three]))

(def scene
  (three/Scene.))

(def camera
  (three/PerspectiveCamera. 75 (/ (.-innerWidth js/window) (.-innerHeight js/window)) 0.1 1000))

(def renderer
  (three/WebGLRenderer.))

(.setSize renderer (.-innerWidth js/window) (.-innerHeight js/window))

(.appendChild (.-body js/document) (.-domElement renderer))

(def geometry
  (three/BoxGeometry. 1 1 1))

(def material
  (three/MeshBasicMaterial. { :color 0x00ff00 }))

(def cube
  (three/Mesh. geometry material))

(.add scene cube)

(set! (.-z (.-position camera)) 5)

(println camera)

(defn animate []
  (set! (.-x (.-rotation cube)) (+ (.-x (.-rotation cube)) 0.01))
  (set! (.-y (.-rotation cube)) (+ (.-y (.-rotation cube)) 0.01))
  (.render renderer scene camera))
(.setAnimationLoop renderer animate)
