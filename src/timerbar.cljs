(ns timerbar
  (:require [util :as util]))

(def timing-padding-px 100)

(defn ^:export timing-y []
  (- (.-innerHeight js/window)
     100))
(defn ^:export timing-to-x [timing duration]
  (let [width (.-innerWidth js/window)
        min-x timing-padding-px
        max-x (- width timing-padding-px)]
    (util/remap timing 0 duration min-x max-x)))

(defn ^:export assemble []
  (let [timing-bar-hud-element (.createElementNS js/document "http://www.w3.org/2000/svg" "circle")]
    (.setAttribute timing-bar-hud-element "r" 5)
    (.setAttribute timing-bar-hud-element "cy" (timing-y))
    {:svg timing-bar-hud-element
     :timerbar {:position 0
                :duration 3000}}))

(defn ^:export update-timerbar-entity [game delta]
  (let [e (:timerbar-entity game)
        svg (.-svg e)
        timerbar (.-timerbar e)
        position (.-position timerbar)
        duration (.-duration timerbar)
        queries (:queries game)
        hitmarker-query (:hitmarker queries)]
    (set! (aget timerbar :position) (+ position delta))
    (when (> position duration)
      (set! (.-position timerbar) 0)
      (doseq [hitmarker-entity hitmarker-query]                  ; spawn marbles, despawn hit markers from last attempt, etc.
        (.remove (:ecs game) hitmarker-entity)))
    (.setAttribute svg "cx" (timing-to-x position duration))))

(defn ^:export assemble-hitmarker [timerbar-entity]
  (let [hud-element (.createElementNS js/document "http://www.w3.org/2000/svg" "circle")]
    (.setAttribute hud-element "r" 5)
    (.setAttribute hud-element "cx" (timing-to-x (-> timerbar-entity :timerbar :position)
                                                 (-> timerbar-entity :timerbar :duration)))
    (.setAttribute hud-element "cy" (timing-y))
    {:svg hud-element
     :hitmarker true}))

