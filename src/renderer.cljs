(ns renderer
  (:require
   ["three" :as three]
   ["three/examples/jsm/controls/OrbitControls" :refer [OrbitControls]]
   ["three/examples/jsm/controls/TransformControls" :refer [TransformControls]]
   [common :as common]
   [ecs :as ecs]
   [input :as input]))

(defn ^:export assemble []
  (let [renderer (three/WebGLRenderer. {:canvas common/canvas})
        scene (three/Scene.)
        camera (three/PerspectiveCamera.
                60
                (/ (.-innerWidth js/window)
                   (.-innerHeight js/window))
                0.1
                5000)
        transform-controls (TransformControls. camera js/document.body)
        orbit-controls (OrbitControls. camera js/document.body)
        loader (three/TextureLoader.)]
    (.load
     loader
     "/empty_play_room_1k.jpg"
     (fn [texture]
       (set! (.-mapping texture) (.-EquirectangularReflectionMapping three))
       (set! (.-colorSpace texture) (.-SRGBColorSpace three))
       (set! (.-background scene) texture)))

    (set! (.. renderer -shadowMap -enabled) true)
    (set! (.-background scene) (three/Color. 0xbfd1e5))

    (set! (.-z (.-position camera)) 100)
    (set! (.-y (.-position camera)) 100)
    (.lookAt camera 0 0 0)

    (.add scene (.getHelper transform-controls))

    ;; (aset orbit-controls :enableDamping true)
    (println orbit-controls)
    ;; TODO: for later
    ;; (aset orbit-controls :minAzimuthAngle -0.3)
    ;; (aset orbit-controls :maxAzimuthAngle 0.3)

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
                :transform-controls transform-controls
                :orbit-controls orbit-controls}}))

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
        camera (:camera renderer-entity)
        transform-controls (:transform-controls renderer-entity)
        orbit-controls (:orbit-controls renderer-entity)]
    (aset orbit-controls :enableRotate (not (aget transform-controls :dragging)))
    ;; (.update orbit-controls)
    ;; (aset (-> camera .-rotation) :x 0)
    (.render renderer scene camera)))

(def reasonable-camera-position (three/Vector3. 7.509209830947526 114.35783395540642 236.268626920854))

(def reasonable-camera-rotation (three/Euler. -0.5593518013037831 0.4265059573765538 0.2534225183136857 "XYZ"))

(defn ^:export handle-reset-camera-to-reasonable-position [game]
  (let [in (ecs/get-single-component game :input)
        renderer (ecs/get-single-component game :renderer)
        controls (:orbit-controls renderer)
        camera (:camera renderer)]
    (when (input/just-key-pressed in "z")
      (.reset controls)
      (-> camera .-position (.copy reasonable-camera-position))
      (-> camera .-rotation (.copy reasonable-camera-rotation)))))
