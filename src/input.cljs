(ns input
  (:require ["three" :as three]
            [ecs :as ecs]))

(def drag-square-threshold (* 10 10))

(defn ^:export assemble []
  (let [mouse-position (three/Vector2.)
        last-mouse-position (three/Vector2.)
        mouse-down {}
        last-mouse-down {}
        mouse-down-start-position {}
        key-pressed {}
        last-key-pressed {}]
    {:input {:mouse-position mouse-position
             :last-mouse-position last-mouse-position
             :mouse-down mouse-down
             :last-mouse-down last-mouse-down
             :mouse-down-start-position mouse-down-start-position
             :key-pressed key-pressed
             :last-key-pressed last-key-pressed
             :pointer (three/Vector2.)
             :raycaster (three/Raycaster.)}}))

(defn ^:export mouse-delta [input]
  (let [{mouse-position :mouse-position
         last-mouse-position :last-mouse-position} input]
    (-> (.clone mouse-position)
        (.sub last-mouse-position))))
(defn ^:export is-mouse-down [input button]
  (get (:mouse-down input) button))
(defn ^:export just-mouse-down [input button]
  (and (get (:mouse-down input) button) (not (get (:last-mouse-down input) button))))
(defn ^:export just-mouse-up [input button]
  (and (get (:last-mouse-down input) button) (not (get (:mouse-down input) button))))
(defn ^:export just-mouse-up-nodrag [input button]
  (and (just-mouse-up input button)
       (<= (.distanceToSquared (get (:mouse-down-start-position input) button) (:mouse-position input))
           drag-square-threshold)))
(defn ^:export is-key-pressed [input key]
  (get (:key-pressed input) key))
(defn ^:export just-key-pressed [input key]
  (and (get (:key-pressed input) key) (not (get (:last-key-pressed input) key))))
(defn ^:export just-key-released [input key]
  (and (get (:last-key-pressed input) key) (not (get (:key-pressed input) key))))

(defn ^:export post-update [game]
  (let [input (ecs/get-single-component game :input)]
    (aset input :last-mouse-position (js/Object.assign {} (:mouse-position input)))
    (aset input :last-mouse-down (js/Object.assign {} (:mouse-down input)))
    (aset input :last-key-pressed (js/Object.assign {} (:key-pressed input)))

    (aset (:pointer input) :x (-> input :mouse-position :x
                                  (/ (.-innerWidth js/window))
                                  (* 2)
                                  (- 1)))
    (aset (:pointer input) :y (-> input :mouse-position :y
                                  (/ (.-innerHeight js/window))
                                  (* 2)
                                  (+ 1)
                                  (-)))))
