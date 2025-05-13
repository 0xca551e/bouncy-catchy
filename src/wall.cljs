(ns wall
  (:require
   ["@dimforge/rapier3d" :as rapier]
   ["three" :as three]
   [common :as common]
   [input :as input]
   [timerbar :as timerbar]))

(defn ^:export handle-object-selection [game]
  (let [camera (:camera game)
        scene (:scene game)
        control (:transform-controls game)]
    (.setFromCamera input/raycaster input/pointer camera)
    (when (input/just-mouse-up-nodrag 0)
      (let* [intersects (.intersectObjects input/raycaster (.-children scene) false)
             first-hit (nth intersects 0)
             controllable (some-> first-hit .-object .-userData .-entity .-controllable)]
            (if (and first-hit controllable)
              (.attach control (.-object first-hit))
              (.detach control))))
    (let [controllable (some-> control .-object .-userData .-entity .-controllable)]
      (when (and controllable (not (.-dragging control)))
        (cond
          (and (:translate controllable) (input/just-key-pressed "g"))
          (set! (.-current controllable) "translate")

          (and (:rotate controllable) (input/just-key-pressed "r"))
          (set! (.-current controllable) "rotate"))
        (.setMode control (.-current controllable))
        (set! (.-showX control) (js/Boolean (-> controllable (get (.-current controllable)) :x)))
        (set! (.-showY control) (js/Boolean (-> controllable (get (.-current controllable)) :y)))
        (set! (.-showZ control) (js/Boolean (-> controllable (get (.-current controllable)) :z)))))))

(defn ^:export assemble-moveable-wall [game dimensions position]
  (let* [physics (:physics game)
         dimensions (.divideScalar (.clone dimensions) common/physics-to-mesh-scaling-factor)
         position (.divideScalar (.clone position) common/physics-to-mesh-scaling-factor)
         half-dimensions (-> (.clone dimensions)
                             (.divideScalar 2))
         geometry (three/BoxGeometry. (:x dimensions)
                                      (:y dimensions)
                                      (:z dimensions))
         material (three/MeshStandardMaterial. {:color 0xffffff})
         mesh (three/Mesh. geometry material)
         collider-desc (-> (.cuboid rapier/ColliderDesc
                                    (:x half-dimensions)
                                    (:y half-dimensions)
                                    (:z half-dimensions))
                           (.setTranslation (:x position)
                                            (:y position)
                                            (:z position)))
         collider (.createCollider physics collider-desc)

         target-time 1000
         duration 3000
         hud-element (.createElementNS js/document "http://www.w3.org/2000/svg" "circle")]
        (.setAttribute hud-element "cx" (timerbar/timing-to-x target-time duration))
        (.setAttribute hud-element "cy" (timerbar/timing-y))
        (.setAttribute hud-element "r" 10)
        (set! (.-receiveShadow mesh) true)
        {:mesh mesh
         :physics collider
         :controllable {:current "translate"
                        :translate {:x true}
                        :rotate {:z true}}
         :instrument true
         :timed-requirement {:duration duration
                             :target-time target-time}
         :svg hud-element}))
