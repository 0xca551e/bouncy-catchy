(ns physics
  (:require
   [common :as common]
   [game :as g]))

(defn ^:export step-physics [game]
  (let [physics (:physics game)
        event-queue (:physics-event-queue game)]
    (.forEach (.-colliders physics) (fn [c]
                                      (let [colliding (some-> c .-userData .-colliding)]
                                        (when (or (= colliding true) (= colliding false))
                                          (set! (.. c -userData -lastcolliding) colliding)))))
    (.step physics event-queue)
    (.drainCollisionEvents event-queue (fn [handle1 handle2 started]
                                         (let [bodies (.-colliders physics)
                                               a (.get bodies handle1)
                                               b (.get bodies handle2)]
                                           (set! (.-userData a) (or (.-userData a) {}))
                                           (set! (.-userData b) (or (.-userData b) {}))
                                           (set! (.. a -userData -colliding) started)
                                           (set! (.. b -userData -colliding) started))))))

(defn ^:export sync-mesh-to-physics [game]
  (let [control (:transform-controls game)
        queries (:queries game)
        mesh-physics-query (:mesh-physics queries)]
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
