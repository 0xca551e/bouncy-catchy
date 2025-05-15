(ns common)

(defn ^:export p [x]
  (println x)
  x)

(defn ^:export lerp [t a b] (+ a (* (- b a) t)))
(defn ^:export inverseLerp [v a b] (/ (- v a) (- b a)))
(defn ^:export remap
  [v inMin inMax outMin outMax]
  (lerp (inverseLerp v inMin inMax) outMin outMax))

(def ^:export app-container
  (.querySelector js/document "#app"))

(def ^:export canvas
  (.querySelector js/document "#canvas"))

(def ^:export hud
  (.querySelector js/document "#hud"))

(def ^:export physics-to-mesh-scaling-factor 100)

(def timing-padding-px 100)
(def timing-bottom-y 100)

(defn ^:export timing-y []
  (- (.-innerHeight js/window)
     timing-bottom-y))
(defn ^:export timing-to-x [timing duration]
  (let [width (.-innerWidth js/window)
        min-x timing-padding-px
        max-x (- width timing-padding-px)]
    (remap timing 0 duration min-x max-x)))
