(ns timerbar
  (:require
   ["three" :as three]
   [common :as common]
   [ecs :as ecs]
   [wall :as wall]))

(defn ^:export assemble [game]
  (let [timing-bar-hud-element (.createElementNS js/document "http://www.w3.org/2000/svg" "circle")]
    (.setAttribute timing-bar-hud-element "r" 5)
    (.setAttribute timing-bar-hud-element "cy" (common/timing-y))
    {:svg timing-bar-hud-element
     :timerbar {:position 0
                :duration 3000
                :level {:walls [(wall/assemble-moveable-wall game (three/Vector3. 100 3 100) (three/Vector3.))]}}}))

(defn ^:export setup-level [game]
  (let [e (ecs/get-single-component game :timerbar)
        level (-> e :level :walls)]
    (doseq [wall level]
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
      (doseq [hitmarker-entity (-> game :queries :hitmarker)]
        ;; spawn marbles, despawn hit markers from last attempt, etc.
        (.remove (:world game) hitmarker-entity)))
    (.setAttribute svg "cx" (common/timing-to-x position duration))))
