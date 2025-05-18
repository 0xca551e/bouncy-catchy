(ns timerbar
  (:require
   ["three" :as three]
   [ball :as ball]
   [common :as common]
   [ecs :as ecs]
   [input :as input]
   [renderer :as renderer]
   [spawner :as spawner]
   [wall :as wall]))

(def timing-error-margin-ms 84)

(def hit-zone-corrections [(three/Vector3. 121.154 -35.461 0)
                           (three/Vector3. 129.766 -4.684 0)
                           (three/Vector3. 131.348 81.981 0)
                           (three/Vector3. 347.838 -16.594 0)])

(defn correction [n v]
  (-> v (.clone) (.sub (nth hit-zone-corrections n))))

(def solutions [[(correction 0 (three/Vector3. 20 0 0))
                 (correction 0 (three/Vector3. 46.53 39.12 0))
                 (correction 0 (three/Vector3. 73.99 -12.773 0))
                 (correction 0 (three/Vector3. 94.39 8.39 0))]
                [(correction 1 (three/Vector3. 83 17.89 25))
                 (correction 1 (three/Vector3. 40.34 -4.66 25))
                 (correction 1 (three/Vector3. 69.85 -23.82 25))]
                [(correction 2 (three/Vector3. 0 0 50))
                 (correction 2 (three/Vector3. 99.33 30.23 50))
                 (correction 2 (three/Vector3. 105.24 96.33 50))
                 (correction 2 (three/Vector3. 118.57 49.54 50))]
                [(correction 3 (three/Vector3. 104 0 75))
                 (correction 3 (three/Vector3. 156.15 57.78 75))
                 (correction 3 (three/Vector3. 156.3 -36.47 75))
                 (correction 3 (three/Vector3. 278.25 -66.51 75))]])

(defn ^:export assemble [game]
  (let [timing-bar-hud-element (.createElementNS js/document "http://www.w3.org/2000/svg" "circle")]
    (.setAttribute timing-bar-hud-element "r" 5)
    (.setAttribute timing-bar-hud-element "cy" (common/timing-y))
    (.setAttribute timing-bar-hud-element "fill" "white")
    (.setAttribute timing-bar-hud-element "stroke" "black")
    {:svg timing-bar-hud-element
     :timerbar {:active true
                :position 0
                :duration 3000
                :current-level -1
                :levels [{:clap-point (three/Vector3.)
                          :walls [(wall/assemble-moveable-wall game "#ff0025" (three/Vector3. 20 2 20) (correction 0 (three/Vector3. 20 0 0)) (three/Euler.) {} {} 816.634 70)
                                  (wall/assemble-moveable-wall game "#0062ff" (three/Vector3. 10 2 10) (correction 0 (three/Vector3. 60 0 0)) (three/Euler.) {:x true :y true} {} (+ 816.634 500) 75)
                                  (wall/assemble-moveable-wall game "#18ff00" (three/Vector3. 20 2 20) (correction 0 (three/Vector3. 100 0 0)) (three/Euler.) {:x true :y true} {} (+ 816.634 1000) 67)
                                  (wall/assemble-moveable-wall game "#fff600" (three/Vector3. 10 2 10) (correction 0 (three/Vector3. 140 0 0)) (three/Euler.) {:x true :y true} {} (+ 816.634 1500) 72)]
                          :clap-time (+ 816.634 2000)
                          :spawner (spawner/assemble (correction 0 (three/Vector3. -35 0 0)) (three/Vector3. 0.7 4 0))}
                         {:clap-point (three/Vector3.)
                          :walls [(wall/assemble-moveable-wall game "#ff0025" (three/Vector3. 20 3 20) (correction 1 (three/Vector3. 94 32 25)) (three/Euler. 0 0 (/ js/Math.PI 2)) {:x true :y true} {} 316.654 78)
                                  (wall/assemble-moveable-wall game "#0062ff" (three/Vector3. 20 3 20) (correction 1 (three/Vector3. 31 -11 25)) (three/Euler. 0 0 (/ js/Math.PI 2)) {:x true :y true} {} (+ 125 316.654) 77)
                                  (wall/assemble-moveable-wall game "#18ff00" (three/Vector3. 20 3 20) (correction 1 (three/Vector3. 64 -45 25)) (three/Euler.) {:x true :y true} {} (+ 250 316.654) 75)]
                          :clap-time 0
                          :spawner (spawner/assemble (correction 1 (three/Vector3. -40 0 25)) (three/Vector3. 4 2 0))}
                         {:center (three/Vector3.)
                          :walls [(wall/assemble-moveable-wall game "#ff0025" (three/Vector3. 20 2 20) (correction 2 (three/Vector3. 0 0 50)) (three/Euler. 0 0 (/ js/Math.PI -6)) {} {} 166.66 66)
                                  (wall/assemble-moveable-wall game "#0062ff" (three/Vector3. 20 2 20) (correction 2 (three/Vector3. 64 26 50)) (three/Euler. 0 0 (/ js/Math.PI 4)) {:x true :y true} {} (+ 250 166.66) 67)
                                  (wall/assemble-moveable-wall game "#18ff00" (three/Vector3. 20 2 20) (correction 2 (three/Vector3. 65 88 50)) (three/Euler. 0 0 (/ js/Math.PI 6)) {:x true :y true} {} (+ 500 166.66) 75)
                                  (wall/assemble-moveable-wall game "#fff600" (three/Vector3. 10 2 10) (correction 2 (three/Vector3. 155 49 50)) (three/Euler.) {:x true :y true} {} (+ 750 166.66) 72)]
                          :clap-time 0
                          :spawner (spawner/assemble (correction 2 (three/Vector3. 0 100 50)) (three/Vector3. 0 -5 0))}
                         {:clap-point (three/Vector3.)
                          :walls [(wall/assemble-moveable-wall game "#ff0025" (three/Vector3. 20 2 20) (correction 3 (three/Vector3. 104 0 75)) (three/Euler.) {} {} 716.638 67)
                                  (wall/assemble-moveable-wall game "#0062ff" (three/Vector3. 20 2 20) (correction 3 (three/Vector3. 146 55 75)) (three/Euler. 0 0 (/ js/Math.PI -4)) {:x true :y true} {} (+ 375 716.638) 75)
                                  (wall/assemble-moveable-wall game "#18ff00" (three/Vector3. 20 2 20) (correction 3 (three/Vector3. 164 -22 75)) (three/Euler. 0 0 (/ js/Math.PI -5)) {:x true :y true} {} (+ 750 716.638) 63)
                                  (wall/assemble-moveable-wall game "#fff600" (three/Vector3. 20 2 20) (correction 3 (three/Vector3. 180 0 75)) (three/Euler. 0 0  (/ js/Math.PI 12)) {:x true :y true} {} (+ 1125 716.638) 60)]
                          :clap-time (+ 500 316.654)
                          :spawner (spawner/assemble (correction 3 (three/Vector3. -40 40 75)) (three/Vector3. 2 3 0))}]
                :hits []
                :modified-during-this-measure false}}))

(defn ^:export handle-responsive-svg [game]
  (let [e (ecs/get-single-component game :timerbar)]
    (doseq [wall (-> e :levels (nth (:current-level e)) :walls)]
      (when (:svg wall)
        (.setAttribute (:svg wall) "cx" (common/timing-to-x (:timed-requirement wall) (:duration e)))
        (.setAttribute (:svg wall) "cy" (common/timing-y))))))

(defn ^:export setup-level [game level-index]
  (let [e (ecs/get-single-component game :timerbar)]
    (aset e :current-level level-index)
    (doseq [wall (-> e :levels (nth level-index) :walls)]
      (.add (:world game) wall))
    (doseq [relative-wall (-> e :levels (nth level-index) :relative-walls)]
      (.add (:world game) relative-wall))))

(defn advance-level [game]
  (let [timerbar (ecs/get-single-component game :timerbar)
        prev-level-index (:current-level timerbar)]
    (if (= prev-level-index (- (.-length (:levels timerbar)) 1))
      (do
        (println "building part done. TODO handle logic to do the rhythm part")
        ; copy pasted from do-solution-skip!!!
        (let* [in (ecs/get-single-component game :input)
               timerbar (ecs/get-single-component game :timerbar)
               current-level (:current-level timerbar)
               solve (nth solutions current-level)]
              (doseq [[i wall] (map-indexed vector (-> timerbar :levels (nth current-level) :walls))]
                (-> wall :physics (.setTranslation (-> (nth solve i) (.clone) (.divideScalar common/physics-to-mesh-scaling-factor))))
                (-> wall :mesh .-material .-color (.setHex (case current-level
                                                             0 0xff9999
                                                             1 0xffff99
                                                             2 0x99ff99
                                                             3 0x9999ff)))

            ;; remove level interaction components for previous level
                )
                                        ;(advance-level game)
              )
        ; "remove" the timerbar now
        (aset timerbar :active false)
        (-> js/document (.querySelector "svg") .-style (aset :display "none"))
        (-> js/document (.querySelector ".fade-overlay") .-style (aset :display "none"))
        (js/alert "Good! The instrument is complete.\nLet's practice using it.\nPress space when the note hits the target circle!\nYou will hear a clap if you time it right. If you miss, you'll hear a record scratch.")
        (-> (ecs/get-single-component game :tutoriallevel) (aset :playing true))
        (renderer/lock-camera game)
        (renderer/reset-camera-to-reasonable-position game)
        (ball/assemble-targets game)
        )
      (do
        ;; remove level interaction components for previous level

        (setup-level game (+ prev-level-index 1))))
    (doseq [wall (-> timerbar :levels (nth prev-level-index) :walls)]
      (.removeComponent (:world game) wall "svg")
      (.removeComponent (:world game) wall "controllable")
      (.removeComponent (:world game) wall "timed-requirement"))))
(defn do-solution-skip [game]
  (let* [in (ecs/get-single-component game :input)
         timerbar (ecs/get-single-component game :timerbar)
         current-level (:current-level timerbar)
         solve (nth solutions current-level)
         renderer (ecs/get-single-component game :renderer)]
    ; detach controls
    (-> renderer :transform-controls (.detach))
    (doseq [[i wall] (map-indexed vector (-> timerbar :levels (nth current-level) :walls))]
      (-> wall :physics (.setTranslation (-> (nth solve i) (.clone) (.divideScalar common/physics-to-mesh-scaling-factor))))
      (-> wall :mesh .-material .-color (.setHex (case current-level
                                                   0 0xff9999
                                                   1 0xffff99
                                                   2 0x99ff99
                                                   3 0x9999ff))))
        (advance-level game)))
(defn ^:export handle-solution-skip [game]
  (when (and (input/just-key-pressed (ecs/get-single-component game :input) "p")
             (:active (ecs/get-single-component game :timerbar)))
    (do-solution-skip game)))
(defn ^:export update-timerbar-entity [game delta]
  (let [e (ecs/get-single game :timerbar)
        svg (.-svg e)
        timerbar (.-timerbar e)
        position (.-position timerbar)
        duration (.-duration timerbar)]
    (set! (aget timerbar :position) (+ position delta))
    (when (and (:active timerbar) (> position duration))
      (set! (.-position timerbar) 0)
      ;; verify hits and set up next level on success
      (when (let [pairs (vec (map vector (:hits timerbar) (-> timerbar :levels (nth (:current-level timerbar)) :walls)))]
              (and
               (not (:modified-during-this-measure timerbar))
               (= (.-length (:hits timerbar)) (.-length (-> timerbar :levels (nth (:current-level timerbar)) :walls)))
               (every? (fn [[hit wall]]
                         (and (= wall (:wall hit))
                              (< (js/Math.abs (- (:time hit)
                                                 (:timed-requirement wall)))
                                 timing-error-margin-ms)))
                       pairs)))
        (do-solution-skip game))
      ;; reset hit verification
      (set! (.-length (:hits timerbar)) 0)
      (aset timerbar :modified-during-this-measure false)
      ;; reset hitmarkers
      (doseq [hitmarker-entity (-> game :queries :hitmarker)]
        (.remove (:world game) hitmarker-entity))
      ;; despawn old marbles
      (doseq [b (-> game :queries :ball)]
        (.remove (:world game) b))
      ;; spawn new marbles
      (spawner/spawn game (-> timerbar :levels (nth (:current-level timerbar)) :spawner)))
    (.setAttribute svg "cx" (common/timing-to-x position duration))
    (.setAttribute svg "cy" (common/timing-y))))
