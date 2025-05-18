(ns ecs
  (:require
   ["@dimforge/rapier3d" :as rapier]
   ["miniplex" :as miniplex]
   [common :as common]))

(defn ^:export get-single [ecs query-name]
  (let [query (-> ecs :queries (get query-name))
        length (-> query .-entities .-length)
        query-is-single (= length 1)]
    (if query-is-single
      (nth query 0)
      (do
        (js/console.error (.join ["expected singleton entity but search matched " length " entities"]))))))

(defn ^:export get-single-component [ecs query-name]
  (get (get-single ecs query-name) query-name))

(defn ^:export make []
  (let [world (miniplex/World.)
        queries {;; singleton queries
                 :input (.with world "input")
                 :midi (.with world "midi")
                 :physics-engine (.with world "physics-engine")
                 :renderer (.with world "renderer")
                 :backingtrack (.with world "backingtrack")
                 :rhythmlevel (.with world "rhythmlevel")
                 :tutoriallevel (.with world "tutoriallevel")
                 ;; regular queries
                 :ball (.with world "ball")
                 :hitmarker (.with world "hitmarker")
                 :instrument (.with world "instrument" "physics")
                 :mesh (.with world "mesh")
                 :mesh-physics (.with world "mesh" "physics")
                 :physics (.with world "physics")
                 :relative-wall (.with world "relative-wall")
                 :svg (.with world "svg")
                 :timerbar (.with world "timerbar")}
        result {:world world
                :queries queries}]
    ;; input
    (.subscribe
     (.-onEntityAdded (:input queries))
     (fn [entity]
       (let [input (:input entity)]
         (js/document.addEventListener
          "contextmenu"
          (fn [e] (.preventDefault e)))
         (js/document.addEventListener
          "mousemove"
          (fn [e]
            (aset (:mouse-position input) :x e.clientX)
            (aset (:mouse-position input) :y e.clientY)))
         (js/document.addEventListener
          "mousedown"
          (fn [e]
            (aset (:mouse-down input) e.button true)
            (aset (:mouse-down-start-position input) e.button (.clone (:mouse-position input)))))
         (js/document.addEventListener "mouseup" (fn [e] (aset (:mouse-down input) e.button false)))
         (js/document.addEventListener "keydown" (fn [e] (aset (:key-pressed input) e.key true)))
         (js/document.addEventListener "keyup" (fn [e] (aset (:key-pressed input) e.key false))))))
    (.subscribe (.-onEntityRemoved (:input queries)) (fn [_entity] (throw "TODO")))
    ;; instrument
    (.subscribe
     (.-onEntityAdded (:instrument queries))
     (fn [e]
       (.setActiveEvents (:physics e) rapier/ActiveEvents.COLLISION_EVENTS)))
    (.subscribe
     (.-onEntityRemoved (:instrument queries))
     (fn [e]
       (.setActiveEvents (:physics e) rapier/ActiveEvents.NONE)))
    ;; mesh
    (.subscribe
     (.-onEntityAdded (:mesh queries))
     (fn [e]
       (let [renderer (get-single-component result :renderer)]
         (.add (:scene renderer) (:mesh e))
         (set! (.-userData (:mesh e)) {:entity e}))))
    (.subscribe
     (.-onEntityRemoved (:mesh queries))
     (fn [e]
       (let [renderer (get-single-component result :renderer)]
         (.remove (:scene renderer) (:mesh e)))))
    ;; mesh-physics
    (.subscribe
     (.-onEntityAdded (:mesh-physics queries))
     (fn [e]
       (.set (.-scale (:mesh e))
             common/physics-to-mesh-scaling-factor
             common/physics-to-mesh-scaling-factor
             common/physics-to-mesh-scaling-factor)))
    ;; physics
    (.subscribe
     (.-onEntityAdded (:physics queries))
     (fn [e]
       (.setEnabled (:physics e) true)
       (set! (.-userData (:physics e)) {:entity e})))
    (.subscribe
     (.-onEntityRemoved (:physics queries))
     (fn [e]
       (let [physics-engine (get-single-component result :physics-engine)]
         (if (.-collider (:physics e))
           (.removeRigidBody (:world physics-engine) (:physics e))
           (.removeCollider (:world physics-engine) (:physics e))))))
    ;; svg
    (.subscribe
     (.-onEntityAdded (:svg queries))
     (fn [e]
       (.appendChild common/hud (:svg e))))
    (.subscribe
     (.-onEntityRemoved (:svg queries))
     (fn [e]
       (.remove (:svg e))))
    ;; -end
    result))
