(ns rhythmlevel
  (:require
   [backingtrack :as backingtrack]
   [ecs :as ecs]
   [input :as input]
   [midi :as midi]
   [spawner :as spawner]))

(def good-timing-window-ms 50)
(def ok-timing-window-ms 100)
(def bad-timing-window-ms 200)

(defn ^:export assemble []
  {:rhythmlevel {:playing true
                 :time 0
                 :spawner-cues [{:time 10000, :spawner 0}
                                {:time 11000, :spawner 1}
                                {:time 12000, :spawner 2}
                                {:time 13000, :spawner 3}
                                ;; just to prevent overflow for now
                                {:time js/Infinity :spawner 0}]
                 :current-spawner-cue 0
                 :clap-times [10000 11000 12000 13000 js/Infinity]
                 :current-clap 0}})

(defn ^:export handle-timing-misses [game]
  (let* [e (ecs/get-single-component game :rhythmlevel)
         current-time (:time e)
         current-clap (:current-clap e)
         current-clap-time (-> e :clap-times (nth (:current-clap e)))
         timing-difference (- current-clap-time current-time)]
    (println current-time)
    (cond
      (<= timing-difference (- bad-timing-window-ms))
      (do
        (println "missed")
        (midi/playsound game 120 0 30 127)
        (aset e :current-clap (+ current-clap 1))))))

(defn ^:export handle-timing-input [game]
  (let* [in (ecs/get-single-component game :input)
         e (ecs/get-single-component game :rhythmlevel)
         current-time (:time e)
         current-clap (:current-clap e)
         current-clap-time (-> e :clap-times (nth (:current-clap e)))
         timing-difference (- current-clap-time current-time)
         timing-offset (js/Math.abs timing-difference)]
        (when (input/just-key-pressed in " ")
          (cond
            (<= timing-offset good-timing-window-ms)
            (do
              (println "good")
              (midi/playsound game 120 0 39 127)
              (aset e :current-clap (+ current-clap 1)))

            (<= timing-offset ok-timing-window-ms)
            (do
              (println "ok")
              (midi/playsound game 120 0 39 127)
              (aset e :current-clap (+ current-clap 1)))

            (<= timing-offset bad-timing-window-ms)
            (do
              (println "bad")
              (midi/playsound game 120 0 30 127)
              (aset e :current-clap (+ current-clap 1)))))))

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
