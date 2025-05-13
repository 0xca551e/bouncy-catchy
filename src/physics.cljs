(ns physics
  (:require
   [common :as common]
   [game :as g]))

(defn ^:export step-physics [game]
  (let [physics (:physics game)
        event-queue (:physics-event-queue game)]
    (.step physics event-queue)
    (.drainCollisionEvents event-queue (fn [handle1 handle2 started]
                                         (when started
                                           (let [bodies (.-colliders physics)
                                                 a (.get bodies handle1)
                                                 b (.get bodies handle2)
                                                 a-instrument (some-> a .-userData .-entity .-instrument)
                                                 b-instrument (some-> b .-userData .-entity .-instrument)]
                                             (println a-instrument b-instrument)
                                             (when a-instrument (g/playsound))
                                             (when b-instrument (g/playsound))))))))

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
