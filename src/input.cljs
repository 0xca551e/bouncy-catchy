(ns input
  (:require ["three" :as three]))

(def ^:export mouse-position (three/Vector2.))
(def last-mouse-position (three/Vector2.))
(defn ^:export mouse-delta []
  (-> (.clone mouse-position)
      (.sub last-mouse-position)))

(def mouse-down {})
(def last-mouse-down {})
(defn ^:export is-mouse-down [button]
  (get mouse-down button))
(defn ^:export just-mouse-down [button]
  (and (aget mouse-down button) (not (aget last-mouse-down button))))
(defn ^:export just-mouse-up [button]
  (and (aget last-mouse-down button) (not (aget mouse-down button))))

(def mouse-down-start-position {})
(def drag-square-threshold (* 10 10))
(defn ^:export just-mouse-up-nodrag [button]
  (and (just-mouse-up button)
       (<= (.distanceToSquared (aget mouse-down-start-position button) mouse-position)
           drag-square-threshold)))

(def key-pressed {})
(def last-key-pressed {})
(defn ^:export is-key-pressed [key]
  (get key-pressed key))
(defn ^:export just-key-pressed [key]
  (and (aget key-pressed key) (not (aget last-key-pressed key))))
(defn ^:export just-key-released [key]
  (and (aget last-key-pressed key) (not (aget key-pressed key))))

(def ^:export pointer (three/Vector2.))
(def ^:export raycaster (three/Raycaster.))

(defn ^:export init []
  (js/document.addEventListener "mousemove" (fn [e]
                                              (set! mouse-position.x e.clientX)
                                              (set! mouse-position.y e.clientY)))
  (js/document.addEventListener "mousedown" (fn [e]
                                              (set! (aget mouse-down e.button) true)
                                              (set! (aget mouse-down-start-position e.button) (.clone mouse-position))))
  (js/document.addEventListener "mouseup" (fn [e] (set! (aget mouse-down e.button) false)))
  (js/document.addEventListener "keydown" (fn [e] (set! (aget key-pressed e.key) true)))
  (js/document.addEventListener "keyup" (fn [e] (set! (aget key-pressed e.key) false))))

(defn ^:export post-update [_game]
  (set! last-mouse-position (js/Object.assign {} mouse-position))
  (set! last-mouse-down (js/Object.assign {} mouse-down))
  (set! last-key-pressed (js/Object.assign {} key-pressed))

  (set! (.-x pointer) (-> (:x mouse-position)
                          (/ (.-innerWidth js/window))
                          (* 2)
                          (- 1)))
  (set! (.-y pointer) (-> (:y mouse-position)
                          (/ (.-innerHeight js/window))
                          (* 2)
                          (+ 1)
                          (-))))
