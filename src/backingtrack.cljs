(ns backingtrack
  (:require [ecs :as ecs]
            [midi :as midi]
            [pianotrack :as pianotrack]))

(defn ^:export assemble []
  {:backingtrack {:notes pianotrack/data
                   :current-index 0}})

(defn ^:export play [game time]
  (let [backing-track (ecs/get-single-component game :backingtrack)]
    ;; (println time)
    (loop []
      (let [current-note (aget (:notes backing-track) (:current-index backing-track))]
        (when (<= (:time current-note) time)
          (if (:on current-note)
            (midi/playsound game 0 5 (:note current-note) (:velocity current-note))
            (midi/stopsound game 0 5 (:note current-note)))
          (aset backing-track :current-index (+ (aget backing-track :current-index) 1))
          (recur))))))
