(ns util)

(defn ^:export lerp [t a b] (+ a (* (- b a) t)))
(defn ^:export inverseLerp [v a b] (/ (- v a) (- b a)))
(defn ^:export remap
  [v inMin inMax outMin outMax]
  (lerp (inverseLerp v inMin inMax) outMin outMax))
