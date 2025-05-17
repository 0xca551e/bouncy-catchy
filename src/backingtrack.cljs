(ns backingtrack
  (:require [ecs :as ecs]
            [midi :as midi]
            [pianotrack :as pianotrack]))

(defn ^:export assemble [data bank program]
  {:backingtrack {:notes data
                  :current-index 0
                  :bank bank
                  :program program}})

(defn ^:export play [game entity time]
  (loop []
    (let* [backing-track (:backingtrack entity)
           current-note (aget (:notes backing-track) (:current-index backing-track))]
      (when (<= (:time current-note) time)
        (if (:on current-note)
          (midi/playsound game 0 5 (:note current-note) (:velocity current-note))
          (midi/stopsound game 0 5 (:note current-note)))
        (aset backing-track :current-index (+ (aget backing-track :current-index) 1))
        (recur)))))
