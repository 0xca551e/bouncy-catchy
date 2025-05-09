(ns main
  (:require ["three" :as three]))

(def scene
  (three/Scene.))

(def camera
  (three/PerspectiveCamera. 75 (/ (.-innerWidth js/window) (.-innerHeight js/window)) 0.1 1000))

(def app-container
  (.querySelector js/document "#app"))

(def canvas
  (.querySelector js/document "#canvas"))

(def renderer
  (three/WebGLRenderer. {:canvas canvas}))

(def geometry
  (three/BoxGeometry. 1 1 1))

(def material
  (three/MeshBasicMaterial. { :color 0x00ff00 }))

(def cube
  (three/Mesh. geometry material))

(.add scene cube)

(set! (.-z (.-position camera)) 5)

(defn animate []
  (let [width (.-clientWidth app-container)
        height (.-clientHeight app-container)]
    (when (or (not= (.-width canvas) width)
              (not= (.-height canvas) height))
      (.setSize renderer width height)
      (set! (.-aspect camera) (/ width height))
      (.updateProjectionMatrix camera)))

  (set! (.-x (.-rotation cube)) (+ (.-x (.-rotation cube)) 0.01))
  (set! (.-y (.-rotation cube)) (+ (.-y (.-rotation cube)) 0.01))
  (.render renderer scene camera))
(.setAnimationLoop renderer animate)
