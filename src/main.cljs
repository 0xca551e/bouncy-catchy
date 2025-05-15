(ns main
  (:require
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

(def accumulator 0)
(def timestep-ms 16.666)
(def last-time 0)
(defn animation-frame [time]
  (let [delta (- time last-time)]
    (set! accumulator (+ accumulator delta))
    (when (> accumulator timestep-ms)
      (set! accumulator (mod accumulator timestep-ms))
      (renderer/resize-to-display-size game)
      (wall/apply-relative-transform game)
      (physics/step-physics game)
      (timerbar/update-timerbar-entity game timestep-ms)
      (physics/sync-mesh-to-physics game)
      (wall/handle-object-selection game)
      (wall/handle-collision game)
      (renderer/render game)
      (input/post-update game)))
  (set! last-time time))

(defn ^:async start []
  (let [audio (js-await (audio/assemble))]
    (.add (:world game) audio))
  (.add (:world game) (input/assemble))
  (.add (:world game) (physics/assemble))
  (.add (:world game) (renderer/assemble))
  (.add (:world game) (timerbar/assemble game))
  (timerbar/setup-level game 0)
  (.setAnimationLoop (-> (ecs/get-single-component game :renderer) :renderer) animation-frame))

(.addEventListener js/document "click" start {:once true})
