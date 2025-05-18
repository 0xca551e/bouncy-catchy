(ns tutoriallevel
  (:require
   [ball :as ball]
   [ecs :as ecs]
   [input :as input]
   [midi :as midi]
   [spawner :as spawner]))

(def good-timing-window-ms 33)
(def ok-timing-window-ms 66)
(def bad-timing-window-ms 200)

(def clap-at (+ 816.634 2000))
(def restart-at (+ clap-at 1000))

(defn ^:export assemble []
  {:tutoriallevel {:playing false
                   :time 0
                   :combo 0
                   :hit false
                   :active-balls []}})

(defn kill-oldest-ball [game e]
  (when (nth (:active-balls e) 0)
    (.remove (:world game) (nth (:active-balls e) 0))
    (.shift (:active-balls e))))

(defn ^:export handle-timing-misses [game]
  (let* [e (ecs/get-single-component game :tutoriallevel)
         current-time (:time e)
         timing-difference (- clap-at current-time)]
    (cond
      (and (not (:hit e)) (<= timing-difference (- bad-timing-window-ms)))
      (do
        (println "missed")
        (aset e :hit true)
        (midi/playsound game 120 0 30 127)
        (aset e :combo 0)
        (kill-oldest-ball game e)))))

(defn ^:export handle-timing-input [game]
  (let* [in (ecs/get-single-component game :input)
         e (ecs/get-single-component game :tutoriallevel)
         current-time (:time e)
         timing-difference (- clap-at current-time)
         timing-offset (js/Math.abs timing-difference)
         current-combo (:combo e)]
        (when (and (:playing e) (input/just-key-pressed in " "))
          (cond
            (<= timing-offset good-timing-window-ms)
            (do
              (println "good")
              (aset e :hit true)
              (midi/playsound game 120 0 39 127)
              (aset e :combo (+ current-combo 1))
              (kill-oldest-ball game e))

            (<= timing-offset ok-timing-window-ms)
            (do
              (println "ok")
              (aset e :hit true)
              (midi/playsound game 120 0 39 127)
              (aset e :combo (+ current-combo 1))
              (kill-oldest-ball game e))

            (<= timing-offset bad-timing-window-ms)
            (do
              (println "bad")
              (aset e :hit true)
              (midi/playsound game 120 0 30 127)
              (aset e :combo 0)
              (kill-oldest-ball game e))))
        (when (>= (:combo e) 3)
          (aset e :playing false)
          (aset e :combo (- js/Infinity))
          (js/setTimeout (fn []
                           (js/alert "Looks like you're ready!\nI'll start the music now.\nDo your best and have fun!"))
                         1000)
          (js/setTimeout (fn []
                           (aset (ecs/get-single-component game :rhythmlevel) :playing true))
                         3000))))

(defn ^:export handle-playback [game delta]
  (let [e (ecs/get-single-component game :tutoriallevel)
        playing (:playing e)]
    (when playing
      (aset e :time (+ (:time e) delta))
      (when (>= (:time e) restart-at)
        (aset e :time 0)
        (aset e :hit false)
        (let* [timerbar (ecs/get-single-component game :timerbar)
               s (-> timerbar :levels (nth 0) :spawner)]
          (.push (:active-balls e) (spawner/spawn game s 0xff3333)))))))
