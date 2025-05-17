(ns timerbar
  (:require
   ["three" :as three]
   [common :as common]
   [ecs :as ecs]
   [spawner :as spawner]
   [wall :as wall]))

(def timing-error-margin-ms 33)

(def solutions [[(three/Vector3. 20 0 0)
                 (three/Vector3. 46.53 39.12 0)
                 (three/Vector3. 73.99 -12.773 0)
                 (three/Vector3. 94.39 8.39 0)]
                [(three/Vector3. 83 17.89 25)
                 (three/Vector3. 40.34 -4.66 25)
                 (three/Vector3. 69.85 -23.82 25)]
                [(three/Vector3. 0 0 75)
                 (three/Vector3. 99.33 30.23 50)
                 (three/Vector3. 105.24 96.33 50)
                 (three/Vector3. 118.57 49.54 50)]
                [(three/Vector3. 104 0 75)
                 (three/Vector3. 156.15 57.78 75)
                 (three/Vector3. 156.3 -36.47 75)
                 (three/Vector3. 278.25 -66.51 75)]])

(defn ^:export assemble [game]
  (let [timing-bar-hud-element (.createElementNS js/document "http://www.w3.org/2000/svg" "circle")]
    (.setAttribute timing-bar-hud-element "r" 5)
    (.setAttribute timing-bar-hud-element "cy" (common/timing-y))
    {:svg timing-bar-hud-element
     :timerbar {:position 0
                :duration 3000
                :current-level -1
                :levels [{:clap-point (three/Vector3.)
                          :walls [(wall/assemble-moveable-wall game (three/Vector3. 20 2 20) (three/Vector3. 20 0 0) (three/Euler.) {} {} 816.634)
                                  (wall/assemble-moveable-wall game (three/Vector3. 10 2 10) (three/Vector3. 60 0 0) (three/Euler.) {:x true :y true} {} (+ 816.634 500))
                                  (wall/assemble-moveable-wall game (three/Vector3. 20 2 20) (three/Vector3. 100 0 0) (three/Euler.) {:x true :y true} {} (+ 816.634 1000))
                                  (wall/assemble-moveable-wall game (three/Vector3. 10 2 10) (three/Vector3. 140 0 0) (three/Euler.) {:x true :y true} {} (+ 816.634 1500))]
                          :clap-time (+ 816.634 2000)
                          :spawner (spawner/assemble (three/Vector3. -35 0 0) (three/Vector3. 0.7 4 0))}
                         {:clap-point (three/Vector3.)
                          :walls [(wall/assemble-moveable-wall game (three/Vector3. 20 3 20) (three/Vector3. 94 32 25) (three/Euler. 0 0 (/ js/Math.PI 2)) {:x true :y true} {} 316.654)
                                  (wall/assemble-moveable-wall game (three/Vector3. 20 3 20) (three/Vector3. 31 -11 25) (three/Euler. 0 0 (/ js/Math.PI 2)) {:x true :y true} {} (+ 125 316.654))
                                  (wall/assemble-moveable-wall game (three/Vector3. 20 3 20) (three/Vector3. 64 -45 25) (three/Euler.) {:x true :y true} {} (+ 250 316.654))]
                          :clap-time 0
                          :spawner (spawner/assemble (three/Vector3. -40 0 25) (three/Vector3. 4 2 0))}
                         {:center (three/Vector3.)
                          :walls [(wall/assemble-moveable-wall game (three/Vector3. 20 2 20) (three/Vector3. 0 0 50) (three/Euler. 0 0 (/ js/Math.PI -6)) {} {} 166.66)
                                  (wall/assemble-moveable-wall game (three/Vector3. 20 2 20) (three/Vector3. 84 26 50) (three/Euler. 0 0 (/ js/Math.PI 4)) {:x true :y true} {} (+ 250 166.66))
                                  (wall/assemble-moveable-wall game (three/Vector3. 20 2 20) (three/Vector3. 95 88 50) (three/Euler. 0 0 (/ js/Math.PI 6)) {:x true :y true} {} (+ 500 166.66))
                                  (wall/assemble-moveable-wall game (three/Vector3. 10 2 10) (three/Vector3. 155 49 50) (three/Euler.) {:x true :y true} {} (+ 750 166.66))]
                          :clap-time 0
                          :spawner (spawner/assemble (three/Vector3. 0 100 50) (three/Vector3. 0 -5 0))}
                         {:clap-point (three/Vector3.)
                          :walls [(wall/assemble-moveable-wall game (three/Vector3. 20 2 20) (three/Vector3. 104 0 75) (three/Euler.) {} {} 716.638)
                                  (wall/assemble-moveable-wall game (three/Vector3. 20 2 20) (three/Vector3. 156 55 75) (three/Euler. 0 0 (/ js/Math.PI -4)) {:x true :y true} {} (+ 375 716.638))
                                  (wall/assemble-moveable-wall game (three/Vector3. 20 2 20) (three/Vector3. 144 -22 75) (three/Euler. 0 0 (/ js/Math.PI -5)) {:x true :y true} {} (+ 750 716.638))
                                  (wall/assemble-moveable-wall game (three/Vector3. 20 2 20) (three/Vector3. 20 0 75) (three/Euler. 0 0  (/ js/Math.PI 12)) {:x true :y true} {} (+ 1125 716.638))]
                          :clap-time (+ 500 316.654)
                          :spawner (spawner/assemble (three/Vector3. -40 40 75) (three/Vector3. 2 3 0))}
                         ]
                :hits []
                :modified-during-this-measure false}}))

(defn ^:export setup-level [game level-index]
  (let [e (ecs/get-single-component game :timerbar)]
    (aset e :current-level level-index)
    (doseq [wall (-> e :levels (nth level-index) :walls)]
      (.setAttribute (:svg wall) "cx" (common/timing-to-x (:timed-requirement wall) (:duration e)))
      (.add (:world game) wall))
    (doseq [relative-wall (-> e :levels (nth level-index) :relative-walls)]
      (.add (:world game) relative-wall))))
(defn advance-level [game]
  (let [timerbar (ecs/get-single-component game :timerbar)
        prev-level-index (:current-level timerbar)]
    ;; remove level interaction components for previous level
    (doseq [wall (-> timerbar :levels (nth prev-level-index) :walls)]
      (println wall)
      (.removeComponent (:world game) wall "svg")
      (.removeComponent (:world game) wall "controllable")
      (.removeComponent (:world game) wall "timed-requirement"))
    ;; TODO: handle cutscene once past the final level (since there is no next level)
    (setup-level game (+ prev-level-index 1))))
(defn ^:export update-timerbar-entity [game delta]
  (let [e (ecs/get-single game :timerbar)
        svg (.-svg e)
        timerbar (.-timerbar e)
        position (.-position timerbar)
        duration (.-duration timerbar)]
    (set! (aget timerbar :position) (+ position delta))
    (when (> position duration)
      (set! (.-position timerbar) 0)
      ;; verify hits and set up next level on success
      (when (let [pairs (vec (map vector (:hits timerbar) (-> timerbar :levels (nth (:current-level timerbar)) :walls)))]
              (and
                  ;; TODO: check if the marble has been caught
               (not (:modified-during-this-measure timerbar))
               (= (.-length (:hits timerbar)) (.-length (-> timerbar :levels (nth (:current-level timerbar)) :walls)))
               (every? (fn [[hit wall]]
                         (and (= wall (:wall hit))
                              (< (js/Math.abs (- (:time hit)
                                                 (:timed-requirement wall)))
                                 timing-error-margin-ms)))
                       pairs)))
        (advance-level game))
      ;; reset hit verification
      (set! (.-length (:hits timerbar)) 0)
      (aset timerbar :modified-during-this-measure false)
      ;; reset hitmarkers
      (doseq [hitmarker-entity (-> game :queries :hitmarker)]
        (.remove (:world game) hitmarker-entity))
      ;; despawn old marbles
      (doseq [ball (-> game :queries :ball)]
        (.remove (:world game) ball))
      ;; spawn new marbles
      (spawner/spawn game (-> timerbar :levels (nth (:current-level timerbar)) :spawner)))
    (.setAttribute svg "cx" (common/timing-to-x position duration))))
