(ns wall
  (:require
   ["@dimforge/rapier3d" :as rapier]
   ["three" :as three]
   [audio :as audio]
   [common :as common]
   [ecs :as ecs]
   [hitmarker :as hitmarker]
   [input :as input]))

(defn ^:export handle-collision [game]
  (let [timerbar-entity (ecs/get-single game :timerbar)]
    (doseq [{physics :physics} (-> game :queries :instrument)]
      (let [user-data (:userData physics)
            colliding (:colliding user-data)
            last-colliding (:lastcolliding user-data)
            just-collided (and colliding (not last-colliding))]
        (when just-collided
          (audio/playsound game)
          (.add (:world game) (hitmarker/assemble timerbar-entity)))))))

(defn ^:export handle-object-selection [game]
  (let [renderer (ecs/get-single-component game :renderer)
        camera (:camera renderer)
        scene (:scene renderer)
        control (:transform-controls renderer)
        input (ecs/get-single-component game :input)
        raycaster (:raycaster input)
        pointer (:pointer input)]
    (.setFromCamera raycaster pointer camera)
    (when (input/just-mouse-up-nodrag input 0)
      (let* [intersects (.intersectObjects raycaster (.-children scene) false)
             first-hit (nth intersects 0)
             controllable (some-> first-hit .-object .-userData .-entity .-controllable)]
        (if (and first-hit controllable)
          (.attach control (.-object first-hit))
          (.detach control))))
    (let [controllable (some-> control .-object .-userData .-entity .-controllable)]
      (when (and controllable (not (.-dragging control)))
        (cond
          (and (:translate controllable) (input/just-key-pressed input "g"))
          (set! (.-current controllable) "translate")

          (and (:rotate controllable) (input/just-key-pressed input "r"))
          (set! (.-current controllable) "rotate"))
        (.setMode control (.-current controllable))
        (set! (.-showX control) (js/Boolean (-> controllable (get (.-current controllable)) :x)))
        (set! (.-showY control) (js/Boolean (-> controllable (get (.-current controllable)) :y)))
        (set! (.-showZ control) (js/Boolean (-> controllable (get (.-current controllable)) :z)))))))

(defn ^:export assemble-moveable-wall [game dimensions position]
  (let* [physics-engine (ecs/get-single-component game :physics-engine)
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
         collider (.createCollider (:world physics-engine) collider-desc)

         target-time 1000
         duration 3000
         hud-element (.createElementNS js/document "http://www.w3.org/2000/svg" "circle")]
        (.setAttribute hud-element "cx" (common/timing-to-x target-time duration))
        (.setAttribute hud-element "cy" (common/timing-y))
        (.setAttribute hud-element "r" 10)
        (.setAttribute hud-element "fill" "#FF0000")
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
