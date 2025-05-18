(ns midi
  (:require ["spessasynth_lib" :as spessasynth]
            [ecs :as ecs]))

(defn ^:async ^:export assemble []
  (let* [context (js/AudioContext.)
         soundfont (js-await (js/fetch "/GeneralUserGS.sf3"))
         soundfont-buffer (js-await (.arrayBuffer soundfont))
         _ (js-await (-> context .-audioWorklet (.addModule "/worklet_processor.min.js")))
         synth (spessasynth/Synthetizer. (.-destination context) soundfont-buffer)
         _ (js-await (.-isReady synth))]
    ;; (js-await (.resume context))
    ;; (.programChange synth 0 48)
    ;; (.noteOn synth 0 52 127)
        {:midi {:context context
                :synth synth}}))

(defn ^:export playsound [game bank program pitch velocity]
  (let [midi (ecs/get-single-component game :midi)
        synth (:synth midi)]
    (.controllerChange synth 0 0 bank)
    (.programChange synth 0 program)
    (.noteOn (:synth midi) 0 pitch velocity)))

(defn ^:export stopsound [game bank program pitch]
  (let [midi (ecs/get-single-component game :midi)
        synth (:synth midi)]
    (.controllerChange synth 0 0 bank)
    (.programChange synth 0 program)
    (.noteOff (:synth midi) 0 pitch)))
