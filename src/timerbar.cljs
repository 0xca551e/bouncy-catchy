(ns timerbar
  (:require
   [common :as common]
   [ecs :as ecs]))

(defn ^:export assemble []
  (let [timing-bar-hud-element (.createElementNS js/document "http://www.w3.org/2000/svg" "circle")]
    (.setAttribute timing-bar-hud-element "r" 5)
    (.setAttribute timing-bar-hud-element "cy" (common/timing-y))
    {:svg timing-bar-hud-element
     :timerbar {:position 0
                :duration 3000}}))

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
