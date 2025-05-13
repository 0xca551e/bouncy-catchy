(ns main
  (:require
   ["three" :as three]
   [ball :as ball]
   [game :refer [initaudio game]]
   [input :as input]
   [physics :as physics]
   [renderer :as renderer]
   [timerbar :as timerbar]
   [wall :as wall]))

(def last-time 0)
(defn animation-frame [time]
  (let [delta (- time last-time)]
    (renderer/resize-to-display-size game)
    (physics/step-physics game)
    (timerbar/update-timerbar-entity game delta)
    (physics/sync-mesh-to-physics game)
    (wall/handle-object-selection game)
    (wall/handle-collision game)
    (renderer/render game)
    (input/post-update game))
  (set! last-time time))

(defn ^:async start []
  (input/init)
  (js-await (initaudio))

  (let [cube (ball/assemble game (three/Vector3. 0 100 0) (three/Vector3. 0 0 0))]
    (.add (:ecs game) cube))

  (let [ground (wall/assemble-moveable-wall game (three/Vector3. 100 3 100) (three/Vector3.))]
    (.add (:ecs game) ground))

  (.setAnimationLoop game/renderer animation-frame))

(.addEventListener
 js/document
 "click"
 (fn []
   (start))
 {:once true})
