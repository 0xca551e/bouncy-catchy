(ns main
  (:require
   ["@dimforge/rapier3d" :as rapier]
   ["miniplex" :as miniplex]
   ["three" :as three]
   ["three/examples/jsm/controls/TransformControls" :refer [TransformControls]]
   [input]))

;; dom objects
(def app-container
  (.querySelector js/document "#app"))

(def canvas
  (.querySelector js/document "#canvas"))

;; game world
(def ecs
  (miniplex/World.))

(def physics-engine
  (let [gravity {:x 0 :y -9.81 :z 0}]
    (rapier/World. gravity)))

;; - setting up threejs world
(def renderer
  (let [r (three/WebGLRenderer. {:canvas main/canvas})]
    (set! (.. r -shadowMap -enabled) true)
    r))

(def scene
  (let [s (three/Scene.)]
    (set! (.-background s) (three/Color. 0xbfd1e5))
    s))

(def camera
  (let [c (three/PerspectiveCamera.
           75
           (/ (.-innerWidth js/window)
              (.-innerHeight js/window))
           0.1
           1000)]
    (set! (.-z (.-position c)) 10)
    (set! (.-y (.-position c)) 10)
    (.lookAt c 0 0 0)
    c))

(def control
  (let [c (TransformControls. camera main/canvas)]
    (.add scene (.getHelper c))
    c))

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

;; queries
(def physics-query (.with ecs "mesh" "physics"))
(.subscribe (.-onEntityAdded physics-query) (fn [e]
                                              (.add scene (:mesh e))))
(.subscribe (.-onEntityRemoved physics-query) (fn [e]
                                                (.remove scene (:mesh e))
                                                (.removeRigidBody (:body (:physics e)))
                                                (.removeCollider (:collider (:physics e)))))

(def controllable-mesh-query (.with ecs "mesh" "controllable"))

;; systems
(defn resize-renderer-to-display-size []
  (let [width (.-clientWidth main/app-container)
        height (.-clientHeight main/app-container)]
    (when (or (not= (.-width main/canvas) width)
              (not= (.-height main/canvas) height))
      (.setSize renderer width height)
      (set! (.-aspect camera) (/ width height))
      (.updateProjectionMatrix camera))))

(defn step-physics []
  (.step physics-engine))

(defn handle-object-selection []
  (.setFromCamera input/raycaster input/pointer camera)
  (when (input/just-mouse-up-nodrag 0)
    (let* [intersects (.intersectObjects input/raycaster (.-children scene) false)
           first-hit (nth intersects 0)]
          (if first-hit
            (.attach control (.-object first-hit))
            nil))))

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
        (.set (.-position mesh)
              (:x t)
              (:y t)
              (:z t))
        (.set (.-quaternion mesh)
              (:x r)
              (:y r)
              (:z r)
              (:w r))))))

(defn render []
  (.render renderer scene camera))

(defn animation-frame []
  (resize-renderer-to-display-size)
  (step-physics)
  (sync-mesh-to-physics)
  (handle-object-selection)
  (render)
  (input/post-update))

;; assemblages
(def ball-radius 0.5)
(defn ^:export assemble-physics-ball [position velocity]
  (let* [;geometry (three/BoxGeometry. 1 1 1)
         geometry (three/SphereGeometry. ball-radius 16 8)
         material (three/MeshStandardMaterial. {:color 0x00ff00})
         mesh (three/Mesh. geometry material)
         rigid-body-desc (-> (.dynamic rapier/RigidBodyDesc)
                             (.setTranslation (:x position)
                                              (:y position)
                                              (:z position))
                             (.setLinvel (:x velocity)
                                         (:y velocity)
                                         (:z velocity)))
         rigid-body (.createRigidBody physics-engine rigid-body-desc)
         ;; collider-desc (.cuboid rapier/ColliderDesc 1 1 1)
         collider-desc (.ball rapier/ColliderDesc ball-radius)
         collider (.createCollider physics-engine collider-desc rigid-body)]
        (set! (.-castShadow mesh) true)
        {:mesh mesh
         :physics {:body rigid-body :collider collider}}))

;; main
(defn start []
  (input/init)

  (let [cube (assemble-physics-ball (three/Vector3. 0 10 0) (three/Vector3.))]
    (.add ecs cube))

   ;; - set up three ground
   ;; TODO: make assemblage
  (let* [geometry (three/BoxGeometry. 10 1 10)
         material (three/MeshStandardMaterial. {:color 0xffffff})
         ground (three/Mesh. geometry material)]
    (set! (.. ground -position -y) 1)
    (set! (.-receiveShadow ground) true)
    (.add scene ground))
  (let [ground-body (rapier/ColliderDesc.cuboid 10 1 10)]
    (.createCollider physics-engine ground-body))

  (.setAnimationLoop renderer animation-frame))

(start)
