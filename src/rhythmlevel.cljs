(ns rhythmlevel
  (:require
   [backingtrack :as backingtrack]
   [ball :as ball]
   [ecs :as ecs]
   [input :as input]
   [midi :as midi]
   [spawner :as spawner]))

(def good-timing-window-ms 33)
(def ok-timing-window-ms 66)
(def bad-timing-window-ms 200)

(def ball-colors [0xff3333 0xffff33 0x33ff33 0x3333ff])

(def spawner-cues [{:time 4000, :spawner 0}
                   {:time 8000, :spawner 0}
                   {:time 12000, :spawner 0}
                   {:time 16000, :spawner 0}
                   {:time 20000, :spawner 3}
                   {:time 22000, :spawner 3}
                   {:time 24000, :spawner 0}
                   {:time 26500, :spawner 2}
                   {:time 28000, :spawner 0}
                   {:time 30500, :spawner 2}
                   {:time 32000, :spawner 0}
                   {:time 35000, :spawner 1}
                   {:time 36000, :spawner 3}
                   {:time 38000, :spawner 3}
                   {:time 40000, :spawner 0}
                   {:time 42500, :spawner 2}
                   {:time 44000, :spawner 2}
                   {:time 45500, :spawner 2}
                   {:time 47000, :spawner 1}
                   {:time 48000, :spawner 2}
                   {:time 49500, :spawner 2}
                   {:time 51000, :spawner 1}
                   {:time 52000, :spawner 3}
                   {:time 54000, :spawner 3}
                   {:time 56000, :spawner 0}
                   {:time 58500, :spawner 2}
                   {:time 60000, :spawner 3}
                   {:time 61500, :spawner 1}
                   {:time 62000, :spawner 3}
                   {:time 63500, :spawner 1}
                   {:time 64000, :spawner 0}
                   {:time 66500, :spawner 2}
                   {:time 68000, :spawner 2}
                   {:time 69000, :spawner 1}
                   {:time 69500, :spawner 2}
                   {:time 70500, :spawner 1}
                   {:time 71000, :spawner 2}
                   {:time 72000, :spawner 2}
                   {:time 73250, :spawner 1}
                   {:time 74000, :spawner 1}
                   {:time 74500, :spawner 1}
                   {:time 75000, :spawner 1}
                   {:time 75500, :spawner 1}
                   ;; just to prevent overflow for now
                   {:time js/Infinity :spawner 0}])

(defn ^:export assemble []
  {:rhythmlevel {:playing false
                 :time 0
                 :calibration [(+ 816.634) (+ 316.654) (+ 166.66) (+ 716.638)]
                 :spawner-cues spawner-cues
                 :current-spawner-cue 0
                 :clap-times (->> spawner-cues (map (fn [x] (case (:spawner x)
                                                                  0 (+ (:time x) 2000)
                                                                  1 (+ (:time x) 500)
                                                                  2 (+ (:time x) 1000)
                                                                  3 (+ (:time x) 1500)))))
                 :current-clap 0
                 :active-balls []}})

(defn kill-oldest-ball [game e]
  (when (nth (:active-balls e) 0)
    (.remove (:world game) (nth (:active-balls e) 0))
    (.shift (:active-balls e))))

(defn ^:export handle-timing-misses [game]
  (let* [e (ecs/get-single-component game :rhythmlevel)
         current-time (:time e)
         current-clap (:current-clap e)
         current-clap-time (-> e :clap-times (nth (:current-clap e)))
         timing-difference (- current-clap-time current-time)]
    (cond
      (<= timing-difference (- bad-timing-window-ms))
      (do
        (println "missed")
        (midi/playsound game 120 0 30 127)
        (aset e :current-clap (+ current-clap 1))
        (kill-oldest-ball game e)))))

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
              (aset e :current-clap (+ current-clap 1))
              (kill-oldest-ball game e))

            (<= timing-offset ok-timing-window-ms)
            (do
              (println "ok")
              (midi/playsound game 120 0 39 127)
              (aset e :current-clap (+ current-clap 1))
              (kill-oldest-ball game e))

            (<= timing-offset bad-timing-window-ms)
            (do
              (println "bad")
              (midi/playsound game 120 0 30 127)
              (aset e :current-clap (+ current-clap 1))
              (kill-oldest-ball game e))))))

(defn ^:export handle-playback [game delta]
  (let [e (ecs/get-single-component game :rhythmlevel)
        playing (:playing e)
        current-spawner-cue (-> e :spawner-cues (nth (:current-spawner-cue e)))]
    (when playing
      (aset e :time (+ (:time e) delta))
      ;; handle spawner cues
      (when (>= (:time e) (- (:time current-spawner-cue) (nth (:calibration e) (:spawner current-spawner-cue))))
        (let* [timerbar (ecs/get-single-component game :timerbar)
               s (-> timerbar :levels (nth (:spawner current-spawner-cue)) :spawner)]
              (.push (:active-balls e) (spawner/spawn game s (nth ball-colors (:spawner current-spawner-cue))))
              (aset e :current-spawner-cue (+ (:current-spawner-cue e) 1))))
      ;; play the midi events
      (doseq [backing-track (-> game :queries :backingtrack)]
        (backingtrack/play game backing-track (:time e))))))
