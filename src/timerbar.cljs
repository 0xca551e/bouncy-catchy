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
        duration (.-duration timerbar)]
    (set! (aget timerbar :position) (+ position delta))
    (when (> position duration)
      (set! (.-position timerbar) 0)
                                        ; spawn marbles, despawn hit markers from last attempt, etc.
      )
    (.setAttribute svg "cx" (timing-to-x position duration))))
