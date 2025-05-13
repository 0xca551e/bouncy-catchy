(ns renderer
  (:require
   [common :as common]))

(defn ^:export resize-to-display-size [game]
  (let [camera (:camera game)
        renderer (:renderer game)
        width (.-clientWidth common/app-container)
        height (.-clientHeight common/app-container)]
    (when (or (not= (.-width common/canvas) width)
              (not= (.-height common/canvas) height))
      (.setSize renderer width height)
      (set! (.-aspect camera) (/ width height))
      (.updateProjectionMatrix camera))))

(defn ^:export render [game]
  (let [camera (:camera game)
        scene (:scene game)
        renderer (:renderer game)]
    (.render renderer scene camera)))
