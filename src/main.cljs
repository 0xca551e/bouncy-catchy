(ns main
  (:require
   ["@dimforge/rapier3d" :as rapier]
   ["miniplex" :as miniplex]
   ["three" :as three]
   ["three/examples/jsm/controls/TransformControls" :refer [TransformControls]]
   [input]))

(def app-container
  (.querySelector js/document "#app"))

(def canvas
  (.querySelector js/document "#canvas"))

(def world
  (let [gravity {:x 0 :y -9.81 :z 0}]
    (rapier/World. gravity)))

(def scene
  (three/Scene.))

(def camera
  (three/PerspectiveCamera. 75 (/ (.-innerWidth js/window) (.-innerHeight js/window)) 0.1 1000))
(set! (.-z (.-position camera)) 10)
(set! (.-y (.-position camera)) 10)
(.lookAt camera 0 0 0)

(def control
  (TransformControls. camera canvas))
(.add scene (.getHelper control))

(def mplexworld
  (miniplex/World.))

(def physics-query (.with mplexworld "mesh" "physics"))
(def controllable-mesh-query (.with mplexworld "mesh" "controllable"))

(.subscribe (.-onEntityAdded physics-query) (fn [e]
                                              (.add scene (:mesh e))))
(.subscribe (.-onEntityRemoved physics-query) (fn [e]
                                                (.remove scene (:mesh e))
                                                (.removeRigidBody (:body (:physics e)))
                                                (.removeCollider (:collider (:physics e)))))

(defn sync-mesh-to-physics []
  (doseq [{mesh :mesh {body :body} :physics} physics-query]
    ;; when the object is under control, we don't want the physics to override our changes to the mesh
    ;; so we do the reverse: sync the physics body to the mesh
    (if (= (.-object control) mesh)
      (let [t (.-position mesh)
            r (.-quaternion mesh)]
        (.setTranslation body t)
        (.setRotation body r))
      (let [t (.translation body)
            r (.rotation body)]
        (.set (.-position mesh) (:x t) (:y t) (:z t))
        (.set (.-quaternion mesh) (:x r) (:y r) (:z r) (:w r))))))

(defn assemble-physics-cube []
  (let* [geometry (three/BoxGeometry. 1 1 1)
         material (three/MeshStandardMaterial. {:color 0x00ff00})
         mesh (three/Mesh. geometry material)
         rigid-body-desc (-> (.dynamic rapier/RigidBodyDesc)
                             (.setTranslation 0 8 0)
                             (.setRotation {:x 0.4254518 :y -0.237339 :z 0.4254518 :w 0.762661}))
         rigid-body (.createRigidBody world rigid-body-desc)
         collider-desc (.cuboid rapier/ColliderDesc 1 1 1)
         collider (.createCollider world collider-desc rigid-body)]
        (set! (.-castShadow mesh) true)
        {:mesh mesh
         :physics {:body rigid-body :collider collider}}))

(set! (.-background scene) (three/Color. 0xbfd1e5))

(def ambient
  (three/HemisphereLight. 0x555555 0xffffff))

(.add scene ambient)

(def light
  (three/DirectionalLight. 0xffffff 4))

(.set (.-position light) -5 10 0)
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

(def renderer
  (three/WebGLRenderer. {:canvas canvas}))

(set! (.. renderer -shadowMap -enabled) true)

(let [cube (assemble-physics-cube)]
  (.add mplexworld cube))

(input/init)

(defn animate []
  (let [width (.-clientWidth app-container)
        height (.-clientHeight app-container)]
    (when (or (not= (.-width canvas) width)
              (not= (.-height canvas) height))
      (.setSize renderer width height)
      (set! (.-aspect camera) (/ width height))
      (.updateProjectionMatrix camera)))
  (.step world)
  (sync-mesh-to-physics)

  (.setFromCamera input/raycaster input/pointer camera)
  (when (input/just-mouse-up-nodrag 0)
    (let* [intersects (.intersectObjects input/raycaster (.-children scene) false)
           first-hit (nth intersects 0)]
      (if first-hit
        (.attach control (.-object first-hit))
        nil)))

  (.render renderer scene camera)

  (input/post-update))
(.setAnimationLoop renderer animate)
