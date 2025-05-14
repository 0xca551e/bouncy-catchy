(ns hitmarker
  (:require [common :as common]))

(defn ^:export assemble [timerbar-entity]
  (let [hud-element (.createElementNS js/document "http://www.w3.org/2000/svg" "circle")]
    (.setAttribute hud-element "r" 5)
    (.setAttribute hud-element "cx" (common/timing-to-x (-> timerbar-entity :timerbar :position)
                                                        (-> timerbar-entity :timerbar :duration)))
    (.setAttribute hud-element "cy" (common/timing-y))
    {:svg hud-element
     :hitmarker true}))
