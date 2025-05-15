(ns physics
  (:require
   ["@dimforge/rapier3d" :as rapier]
   [common :as common]
   [ecs :as ecs]))

(defn ^:export assemble []
  (let [gravity {:x 0 :y -9.81 :z 0}
        world (rapier/World. gravity)]
    {:physics-engine {:world world
                      :event-queue (rapier/EventQueue. true)}}))

(defn ^:export step-physics [game]
  (let [physics-engine (ecs/get-single-component game :physics-engine)
        world (:world physics-engine)
        event-queue (:event-queue physics-engine)]
    (.forEach (.-colliders world) (fn [c]
                                    (let [colliding (some-> c .-userData .-colliding)]
                                      (when (or (= colliding true) (= colliding false))
                                        (set! (.. c -userData -lastcolliding) colliding)))))
    (.step world event-queue)
    (.drainCollisionEvents event-queue (fn [handle1 handle2 started]
                                         (let [bodies (.-colliders world)
                                               a (.get bodies handle1)
                                               b (.get bodies handle2)]
                                           (set! (.-userData a) (or (.-userData a) {}))
                                           (set! (.-userData b) (or (.-userData b) {}))
                                           (set! (.. a -userData -colliding) started)
                                           (set! (.. b -userData -colliding) started))))))

(defn ^:export sync-mesh-to-physics [game]
  (let [control (:transform-controls (ecs/get-single-component game :renderer))
        mesh-physics-query (-> game :queries :mesh-physics)]
    (doseq [{mesh :mesh body :physics} mesh-physics-query]
      ;; when the object is a controllable wall, we don't want the physics to override our changes to the mesh
      ;; so we do the reverse: sync the physics body to the mesh
      (if (= (.-object control) mesh)
        (let [t (.-position mesh)
              r (.-quaternion mesh)]
          (.setTranslation body (.divideScalar (.clone t) common/physics-to-mesh-scaling-factor))
          (.setRotation body r))
        (let [t (.translation body)
              r (.rotation body)]
          (.set (.-position mesh)
                (* common/physics-to-mesh-scaling-factor (:x t))
                (* common/physics-to-mesh-scaling-factor (:y t))
                (* common/physics-to-mesh-scaling-factor (:z t)))
          (.set (.-quaternion mesh)
                (:x r)
                (:y r)
                (:z r)
                (:w r)))))))
