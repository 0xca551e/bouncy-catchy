(ns ball
  (:require
   ["@dimforge/rapier3d" :as rapier]
   ["three" :as three]
   [common :as common]
   [ecs :as ecs]))

(def ball-radius 0.01)
(defn ^:export assemble [game position velocity]
  (let* [physics (ecs/get-single-component game :physics-engine)
         position (.divideScalar (.clone position) common/physics-to-mesh-scaling-factor)
         geometry (three/SphereGeometry. ball-radius 16 8)
         material (three/MeshStandardMaterial. {:color 0x00ff00})
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
    (set! (.-castShadow mesh) true)
    {:mesh mesh
     :physics rigid-body
     :ball true}))
