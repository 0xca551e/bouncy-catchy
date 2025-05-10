(ns main
  (:require ["three" :as three]
            ["@dimforge/rapier3d" :as rapier]))

(def world
  (let [gravity { :x 0 :y -9.81 :z 0 }]
    (rapier/World. gravity)))

(def scene
  (three/Scene.))

(set! (.-background scene) (three/Color. 0xbfd1e5))

(def camera
  (three/PerspectiveCamera. 75 (/ (.-innerWidth js/window) (.-innerHeight js/window)) 0.1 1000))

(def ambient
  (three/HemisphereLight. 0x555555 0xffffff))

(.add scene ambient)

(def light
  (three/DirectionalLight. 0xffffff 4))

(.set (.-position light) 0 10 0)
(set! (.-castShadow light) true)
(set! (.. light -shadow -radius) 3)
(set! (.. light -shadow -blurSamples) 8)
(set! (.. light -shadow -mapSize -width) 1024)
(set! (.. light -shadow -mapSize -height) 1024)
(let [size 10]
  (set! (.. light -shadow -camera -left) (- size))
  (set! (.. light -shadow -camera -bottom) (- size))
  (set! (.. light -shadow -camera -right) size)
  (set! (.. light -shadow -camera -top) size)
  (set! (.. light -shadow -camera -near) 1)
  (set! (.. light -shadow -camera -far) 50))
(.add scene light)

(def ground
  (let [geometry (three/BoxGeometry. 10 1 10)
        material (three/MeshStandardMaterial. { :color 0xffffff })]
    (three/Mesh. geometry material)))
(let [ground-body (rapier/ColliderDesc.cuboid 10 1 10)]
  (.createCollider world ground-body))

;; TODO: ground physics object is off by 1?
(set! (.. ground -position -y) 1)

(set! (.-receiveShadow ground) true)
(.add scene ground)

(def app-container
  (.querySelector js/document "#app"))

(def canvas
  (.querySelector js/document "#canvas"))

(def renderer
  (three/WebGLRenderer. {:canvas canvas}))

(set! (.. renderer -shadowMap -enabled) true)

(def geometry
  (three/BoxGeometry. 1 1 1))

(def material
  (three/MeshStandardMaterial. { :color 0x00ff00 }))

(def cube
  (three/Mesh. geometry material))
(def cube-body
  (let [cube-body-desc (-> (.dynamic rapier/RigidBodyDesc)
                           (.setTranslation 0 8 0)
                           (.setRotation {:x 0.4254518 :y -0.237339 :z 0.4254518 :w 0.762661}))]
    (.createRigidBody world cube-body-desc)))

(let [collider-desc (.cuboid rapier/ColliderDesc 1 1 1)]
  (.createCollider world collider-desc cube-body))

(set! (.-castShadow cube) true)
;; (.set (.-position cube) 0 3 0)
(.add scene cube)

(set! (.-z (.-position camera)) 10)
(set! (.-y (.-position camera)) 10)
(.lookAt camera 0 0 0)

(defn animate []
  (let [width (.-clientWidth app-container)
        height (.-clientHeight app-container)]
    (when (or (not= (.-width canvas) width)
              (not= (.-height canvas) height))
      (.setSize renderer width height)
      (set! (.-aspect camera) (/ width height))
      (.updateProjectionMatrix camera)))
  (.step world)
  (let [t (.translation cube-body)
        r (.rotation cube-body)]
    (.set (.-position cube) (:x t) (:y t) (:z t))
    (.set (.-quaternion cube) (:x r) (:y r) (:z r) (:w r)))
  (.render renderer scene camera))
(.setAnimationLoop renderer animate)
