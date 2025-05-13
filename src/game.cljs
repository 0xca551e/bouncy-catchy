(ns game
  (:require
   ["@dimforge/rapier3d" :as rapier]
   ["miniplex" :as miniplex]
   ["three" :as three]
   ["three/examples/jsm/controls/TransformControls" :refer [TransformControls]]
   [common :as common]
   [timerbar :as timerbar]))

;; web audio
(def audio-context nil)
(def response nil)
(def array-buffer nil)
(def audio-buffer nil)

(defn ^:export playsound []
  (let [source (.createBufferSource audio-context)]
    (set! (.-buffer source) audio-buffer)
    (.connect source (.-destination audio-context))
    (.start source)))

(defn ^:export ^:async initaudio []
  (set! audio-context (js/AudioContext.))
  (set! response
        (js-await (js/fetch "/696317__mandaki__burmese-xylophone-sample.mp3")))
  (set! array-buffer (js-await (.arrayBuffer response)))
  (set! audio-buffer (js-await (.decodeAudioData audio-context array-buffer))))

;; physics
(def physics (let [gravity {:x 0 :y -9.81 :z 0}] (rapier/World. gravity)))
(def physics-event-queue (rapier/EventQueue. true))

;; 3d renderer
(def renderer (three/WebGLRenderer. {:canvas common/canvas}))
(set! (.. renderer -shadowMap -enabled) true)
(def scene (three/Scene.))
(set! (.-background scene) (three/Color. 0xbfd1e5))
(def camera (three/PerspectiveCamera.
                      75
                      (/ (.-innerWidth js/window)
                         (.-innerHeight js/window))
                      0.1
                      1000))
(set! (.-z (.-position camera)) 100)
(set! (.-y (.-position camera)) 100)
(.lookAt camera 0 0 0)

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

;; ecs
(def ecs (miniplex/World.))

(def mesh-physics-query (.with ecs "mesh" "physics"))
(.subscribe (.-onEntityAdded mesh-physics-query) (fn [e]
                                                   (.set (.-scale (:mesh e))
                                                         common/physics-to-mesh-scaling-factor
                                                         common/physics-to-mesh-scaling-factor
                                                         common/physics-to-mesh-scaling-factor)))

(def physics-query (.with ecs "physics"))
(.subscribe (.-onEntityAdded physics-query) (fn [e]
                                              (set! (.-userData (:physics e)) {:entity e})))
(.subscribe (.-onEntityRemoved physics-query) (fn [e]
                                                (if (.-collider (:physics e))
                                                  (.removeRigidBody physics (:physics e))
                                                  (.removeCollider physics (:physics e)))))

(def mesh-query (.with ecs "mesh"))
(.subscribe (.-onEntityAdded mesh-query) (fn [e]
                                           (.add scene (:mesh e))
                                           (set! (.-userData (:mesh e)) {:entity e})))
(.subscribe (.-onEntityRemoved mesh-query) (fn [e]
                                             (.remove scene (:mesh e))))

(def svg-query (.with ecs "svg"))
(.subscribe (.-onEntityAdded svg-query) (fn [e]
                                          (.appendChild common/hud (:svg e))))
(.subscribe (.-onEntityRemoved svg-query) (fn [e]
                                            (.remove (:svg e))))

(def instrument-query (.with ecs "physics" "instrument"))
(.subscribe (.-onEntityAdded instrument-query) (fn [e]
                                                 (.setActiveEvents (:physics e) rapier/ActiveEvents.COLLISION_EVENTS)))
(.subscribe (.-onEntityRemoved instrument-query) (fn [e]
                                                   (.setActiveEvents (:physics e) rapier/ActiveEvents.NONE)))

(def hitmarker-query (.with ecs "hitmarker"))

(def timerbar-entity (timerbar/assemble))
(.add ecs timerbar-entity)

(def transform-controls
  (let [c (TransformControls. camera js/document.body)]
    (.add scene (.getHelper c))
    c))

(def ^:export game
  {:physics physics
   :physics-event-queue physics-event-queue
   :renderer renderer
   :scene scene
   :camera camera
   :ecs ecs
   :queries {:mesh-physics mesh-physics-query
             :physics physics-query
             :mesh mesh-query
             :svg svg-query
             :instrument instrument-query
             :hitmarker hitmarker-query}
   :timerbar-entity timerbar-entity
   :transform-controls transform-controls})
