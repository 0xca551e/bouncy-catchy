(ns ball
  (:require
   ["@dimforge/rapier3d" :as rapier]
   ["three" :as three]
   [common :as common]
   [ecs :as ecs]))

(def ball-radius 0.012)
(defn ^:export assemble [game position velocity color]
  (let* [physics (ecs/get-single-component game :physics-engine)
         position (.divideScalar (.clone position) common/physics-to-mesh-scaling-factor)
         geometry (three/SphereGeometry. ball-radius 16 8)
         material (three/MeshStandardMaterial. {:color 0xffffff})
         mesh (three/Mesh. geometry material)
         rigid-body-desc (-> (.dynamic rapier/RigidBodyDesc)
                             (.setTranslation (:x position)
                                              (:y position)
                                              (:z position))
                             (.setLinvel (:x velocity)
                                         (:y velocity)
                                         (:z velocity))
                             (.setCcdEnabled true))
         rigid-body (.createRigidBody (:world physics) rigid-body-desc)
         collider-desc (-> (.ball rapier/ColliderDesc ball-radius)
                           (.setRestitution 0.8)
                           (.setRestitutionCombineRule rapier/CoefficientCombineRule.Max))
         _collider (.createCollider (:world physics) collider-desc rigid-body)]
        (when color
          (.add mesh (three/LineLoop. (three/CircleGeometry. 0.1 32) (three/LineBasicMaterial. {:color color, :linewidth 2}))))
        (.setEnabled rigid-body false)
        (set! (.-castShadow mesh) true)
        {:mesh mesh
         :physics rigid-body
         :ball true}))

(defn ^:export assemble-target [position color]
  (let* [geometry (three/CircleGeometry. 10 32)
         material (three/LineBasicMaterial. {:color color, :linewidth 2})
         mesh (three/LineLoop. geometry material)]
    (-> mesh .-position (.copy position))
    (set! (.-castShadow mesh) true)
        {:mesh mesh
         :ball-target true}))

(defn ^:export assemble-targets [game]
  (.add (:world game) (ball/assemble-target (three/Vector3. 0 0 0) 0xff9999))
  (.add (:world game) (ball/assemble-target (three/Vector3. 0 0 25) 0xffff99))
  (.add (:world game) (ball/assemble-target (three/Vector3. 0 0 50) 0x99ff99))
  (.add (:world game) (ball/assemble-target (three/Vector3. 0 0 75) 0x9999ff)))
