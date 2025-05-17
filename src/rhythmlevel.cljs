(ns rhythmlevel
  (:require
   [backingtrack :as backingtrack]
   [ecs :as ecs]
   [spawner :as spawner]))

(defn ^:export assemble []
  {:rhythmlevel {:playing true
                 :time 0
                 :spawner-cues [{:time 10000, :spawner 0}
                                {:time 11000, :spawner 1}
                                {:time 12000, :spawner 2}
                                {:time 13000, :spawner 3}
                                ;; just to prevent overflow for now
                                {:time js/Infinity :spawner 0}]
                 :current-spawner-cue 0}})

(defn ^:export handle-playback [game delta]
  (let [e (ecs/get-single-component game :rhythmlevel)
        playing (:playing e)
        current-spawner-cue (-> e :spawner-cues (nth (:current-spawner-cue e)))]
    (when playing
      (aset e :time (+ (:time e) delta))
      ;; handle spawner cues
      (when (>= (:time e) (:time current-spawner-cue))
        (let* [timerbar (ecs/get-single-component game :timerbar)
               s (-> timerbar :levels (nth (:spawner current-spawner-cue)) :spawner)]
              (aset e :current-spawner-cue (+ (:current-spawner-cue e) 1))
              (spawner/spawn game s)))
      ;; play the midi events
      (doseq [backing-track (-> game :queries :backingtrack)]
        (backingtrack/play game backing-track (:time e))))))
