(ns timerbar
  (:require
   ["three" :as three]
   [common :as common]
   [ecs :as ecs]
   [spawner :as spawner]
   [wall :as wall]))

(def timing-error-margin-ms 33)

(defn ^:export assemble [game]
  (let [timing-bar-hud-element (.createElementNS js/document "http://www.w3.org/2000/svg" "circle")]
    (.setAttribute timing-bar-hud-element "r" 5)
    (.setAttribute timing-bar-hud-element "cy" (common/timing-y))
    {:svg timing-bar-hud-element
     :timerbar {:position 0
                :duration 3000
                :level {:walls [(wall/assemble-moveable-wall game (three/Vector3. 100 3 100) (three/Vector3.) 1000)
                                (wall/assemble-moveable-wall game (three/Vector3. 50 3 50) (three/Vector3. 100 0 0) 2000)]
                        :spawner (spawner/assemble (three/Vector3. -100 3 0) (three/Vector3. 1 4 0))}
                :hits []}}))

(defn ^:export setup-level [game]
  (let [e (ecs/get-single-component game :timerbar)
        level (-> e :level :walls)]
    (doseq [wall level]
      (.setAttribute (:svg wall) "cx" (common/timing-to-x (:timed-requirement wall) (:duration e)))
      (.add (:world game) wall))))

(defn ^:export update-timerbar-entity [game delta]
  (let [e (ecs/get-single game :timerbar)
        svg (.-svg e)
        timerbar (.-timerbar e)
        position (.-position timerbar)
        duration (.-duration timerbar)]
    (set! (aget timerbar :position) (+ position delta))
    (when (> position duration)
      (set! (.-position timerbar) 0)
      ;; verify hits
      (println (let [pairs (vec (map vector (:hits timerbar) (-> timerbar :level :walls)))]
                 (and ;; TODO: also check if user moved something around this loop
           ;; TODO: check if the marble has been caught
                  (= (.-length (:hits timerbar)) (.-length (-> timerbar :level :walls)))
                  (every? (fn [[hit wall]]
                            (and (= wall (:wall hit))
                                 (< (js/Math.abs (- (:time hit)
                                                    (:timed-requirement wall)))
                                    timing-error-margin-ms)))
                          pairs))))
      ;; reset hit verification
      (set! (.-length (:hits timerbar)) 0)
      ;; reset hitmarkers
      (doseq [hitmarker-entity (-> game :queries :hitmarker)]
        (.remove (:world game) hitmarker-entity))
      ;; despawn old marbles
      (doseq [ball (-> game :queries :ball)]
        (.remove (:world game) ball))
      ;; spawn new marbles
      (spawner/spawn game (-> e :timerbar :level :spawner)))
    (.setAttribute svg "cx" (common/timing-to-x position duration))))
