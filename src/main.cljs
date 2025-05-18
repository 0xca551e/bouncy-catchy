(ns main
  (:require
   [backingtrack :as backingtrack]
   [common :as common]
   [drumtrack]
   [ecs :as ecs]
   [input :as input]
   [midi :as midi]
   [physics :as physics]
   [pianotrack :as pianotrack]
   [renderer :as renderer]
   [rhythmlevel :as rhythmlevel]
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
      ;; (backingtrack/play game time)
      (set! accumulator (mod accumulator timestep-ms))
      (renderer/resize-to-display-size game)
      (wall/apply-relative-transform game)
      (physics/step-physics game)
      (rhythmlevel/handle-playback game timestep-ms)
      (rhythmlevel/handle-timing-misses game)
      (rhythmlevel/handle-timing-input game)
      (timerbar/update-timerbar-entity game timestep-ms)
      (timerbar/handle-solution-skip game)
      (timerbar/handle-responsive-svg game)
      (physics/sync-mesh-to-physics game)
      (wall/handle-object-selection game)
      (wall/handle-collision game)
      (renderer/render game)
      (input/post-update game)))
  (set! last-time time))

(defn ^:async start []
  (-> common/intro-container .-classList (.add "intro--fade-out"))
  (let [midi (js-await (midi/assemble))]
    (.add (:world game) midi))
  (.add (:world game) (input/assemble))
  (.add (:world game) (physics/assemble))
  (.add (:world game) (renderer/assemble))
  (.add (:world game) (timerbar/assemble game))
  (.add (:world game) (backingtrack/assemble pianotrack/data 0 5))
  (.add (:world game) (backingtrack/assemble drumtrack/data 120 0))
  (.add (:world game) (rhythmlevel/assemble))
  (timerbar/setup-level game 0)
  (.setAnimationLoop (-> (ecs/get-single-component game :renderer) :renderer) animation-frame))

(.addEventListener common/start-button "click" start {:once true})
