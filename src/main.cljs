(ns main
  (:require
   ["miniplex" :as miniplex]
   ["three" :as three]
   [audio :as audio]
   [ecs :as ecs]
   [ball :as ball]
   [input :as input]
   [physics :as physics]
   [renderer :as renderer]
   [timerbar :as timerbar]
   [wall :as wall]))

(def game (ecs/make))

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
  (let [audio (js-await (audio/assemble))]
    (.add (:world game) audio))
  (.add (:world game) (input/assemble))
  (.add (:world game) (physics/assemble))
  (.add (:world game) (renderer/assemble))
  (.add (:world game) (timerbar/assemble game))
  (let [cube (ball/assemble game (three/Vector3. 0 100 0) (three/Vector3. 0 0 0))]
    (.add (:world game) cube))
  (timerbar/setup-level game)
  (.setAnimationLoop (-> (ecs/get-single-component game :renderer) :renderer) animation-frame))

(.addEventListener js/document "click" start {:once true})
