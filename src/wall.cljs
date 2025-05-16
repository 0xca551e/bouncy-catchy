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
    (doseq [e (-> game :queries :instrument)]
      (let [physics (:physics e)
            user-data (:userData physics)
            colliding (:colliding user-data)
            last-colliding (:lastcolliding user-data)
            just-collided (and colliding (not last-colliding))]
        (when just-collided
          (audio/playsound game)
          (-> timerbar-entity :timerbar :hits (.push {:wall e :time (-> timerbar-entity :timerbar :position common/p)}))
          (when (:timed-requirement e)
            (.add (:world game) (hitmarker/assemble timerbar-entity))))))))

(defn ^:export handle-object-selection [game]
  (let [renderer (ecs/get-single-component game :renderer)
        camera (:camera renderer)
        scene (:scene renderer)
        control (:transform-controls renderer)
        orbit-control (:orbit-controls renderer)
        input (ecs/get-single-component game :input)
        raycaster (:raycaster input)
        pointer (:pointer input)
        timerbar (ecs/get-single-component game :timerbar)]
    (.setFromCamera raycaster pointer camera)
    (when (input/just-mouse-up-nodrag input 0)
      (let* [intersects (.intersectObjects raycaster (.-children scene) false)
             first-hit (nth intersects 0)
             controllable (some-> first-hit .-object .-userData .-entity .-controllable)]
            (if (and first-hit controllable)
              (.attach control (-> first-hit .-object common/p))
              (.detach control))))
    (let [controllable (some-> control .-object .-userData .-entity .-controllable)]
      (when (and controllable (.-dragging control))
        (aset timerbar :modified-during-this-measure true))
      (when (and controllable (not (.-dragging control)))
        (cond
          (and (:translate controllable) (input/just-key-pressed input "g"))
          (set! (.-current controllable) "translate")

          (and (:rotate controllable) (input/just-key-pressed input "r"))
          (set! (.-current controllable) "rotate")

          (input/just-key-pressed input "f")
          (aset orbit-control :target (-> control .-object .-userData .-entity :mesh .-position .clone)))
        (.setMode control (.-current controllable))
        (set! (.-showX control) (js/Boolean (-> controllable (get (.-current controllable)) :x)))
        (set! (.-showY control) (js/Boolean (-> controllable (get (.-current controllable)) :y)))
        (set! (.-showZ control) (js/Boolean (-> controllable (get (.-current controllable)) :z)))))))

;; TODO: all relative walls have a relative spawner counterpart, but you can re-use assemblespawner in timerbar.cljs
(defn ^:export assemble-relative-wall [game dimensions parent offset center angle]
  (let* [physics-engine (ecs/get-single-component game :physics-engine)
         dimensions (.divideScalar (.clone dimensions) common/physics-to-mesh-scaling-factor)
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
                                    (:z half-dimensions)))
         collider (.createCollider (:world physics-engine) collider-desc)]
        (set! (.-receiveShadow mesh) true)
        (.setEnabled collider false)
        {:mesh mesh
         :physics collider
         :instrument true
         :relative-wall {:parent parent
                         :offset offset
                         :center center
                         :angle angle}}))

(defn ^:export apply-relative-transform [game]
  (doseq [wall (-> game :queries :relative-wall)]
    (let* [mesh (:mesh wall)
           relative-wall (:relative-wall wall)
           parent (:parent relative-wall)
           transformed (-> parent
                           (aget :mesh)
                           (aget :position)
                           (.clone)
                           (.sub (:center relative-wall))
                           (.applyAxisAngle (three/Vector3. 0 1 0) (:angle relative-wall))
                           (.add (:center relative-wall))
                           (.add (:offset relative-wall)))]
          (.copy (.-position mesh) transformed)
          (-> mesh
              .-quaternion
              (.copy (-> parent
                         :mesh
                         .-quaternion)))
          (.rotateY mesh (:angle relative-wall)))))

(defn ^:export assemble-moveable-wall [game dimensions position rotation translate-controls rotate-controls target-time]
  (let* [physics-engine (ecs/get-single-component game :physics-engine)
         dimensions (.divideScalar (.clone dimensions) common/physics-to-mesh-scaling-factor)
         position (.divideScalar (.clone position) common/physics-to-mesh-scaling-factor)
         rotation (-> (three/Quaternion.) (.setFromEuler rotation))
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
                                            (:z position))
                           (.setRotation rotation))
         collider (.createCollider (:world physics-engine) collider-desc)

         hud-element (.createElementNS js/document "http://www.w3.org/2000/svg" "circle")]
        (.setAttribute hud-element "cy" (common/timing-y))
        (.setAttribute hud-element "r" 10)
        (.setAttribute hud-element "fill" "#FF0000")
        (set! (.-receiveShadow mesh) true)
        (.setEnabled collider false)
        {:mesh mesh
         :physics collider
         :controllable {:current "translate"
                        :translate translate-controls
                        :rotate rotate-controls}
         :instrument true
         :timed-requirement target-time
         :svg hud-element}))
