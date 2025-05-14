(ns renderer
  (:require
   ["three" :as three]
   ["three/examples/jsm/controls/TransformControls" :refer [TransformControls]]
   [common :as common]
   [ecs :as ecs]))

(defn ^:export assemble []
  (let [renderer (three/WebGLRenderer. {:canvas common/canvas})
        scene (three/Scene.)
        camera (three/PerspectiveCamera.
                75
                (/ (.-innerWidth js/window)
                   (.-innerHeight js/window))
                0.1
                1000)
        transform-controls (TransformControls. camera js/document.body)]
    (set! (.. renderer -shadowMap -enabled) true)
    (set! (.-background scene) (three/Color. 0xbfd1e5))

    (set! (.-z (.-position camera)) 100)
    (set! (.-y (.-position camera)) 100)
    (.lookAt camera 0 0 0)

    (.add scene (.getHelper transform-controls))

    (let [ambient-light (three/HemisphereLight. 0x555555 0xffffff)]
      (.add scene ambient-light))

    (let [sunlight (three/DirectionalLight. 0xffffff 4)]
      (.set (.-position sunlight) -5 10 0)
      (set! (.-castShadow sunlight) true)
      (set! (.. sunlight -shadow -radius) 3)
      (set! (.. sunlight -shadow -blurSamples) 8)
      (set! (.. sunlight -shadow -mapSize -width) 1024)
      (set! (.. sunlight -shadow -mapSize -height) 1024)
      (let [size 10]
        (set! (.. sunlight -shadow -camera -left) (- size))
        (set! (.. sunlight -shadow -camera -bottom) (- size))
        (set! (.. sunlight -shadow -camera -right) size)
        (set! (.. sunlight -shadow -camera -top) size)
        (set! (.. sunlight -shadow -camera -near) 1)
        (set! (.. sunlight -shadow -camera -far) 50))
      (.add scene sunlight))
    {:renderer {:renderer renderer
                :scene scene
                :camera camera
                :transform-controls transform-controls}}))

(defn ^:export resize-to-display-size [game]
  (let [renderer (ecs/get-single-component game :renderer)
        camera (:camera renderer)
        renderer (:renderer renderer)
        width (.-clientWidth common/app-container)
        height (.-clientHeight common/app-container)]
    (when (or (not= (.-width common/canvas) width)
              (not= (.-height common/canvas) height))
      (.setSize renderer width height)
      (set! (.-aspect camera) (/ width height))
      (.updateProjectionMatrix camera))))

(defn ^:export render [game]
  (let [renderer-entity (ecs/get-single-component game :renderer)
        renderer (:renderer renderer-entity)
        scene (:scene renderer-entity)
        camera (:camera renderer-entity)]
    (.render renderer scene camera)))
