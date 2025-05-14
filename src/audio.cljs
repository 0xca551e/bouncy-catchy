(ns audio
  (:require [ecs :as ecs]))

(defn ^:export ^:async assemble []
  (let [audio-context (js/AudioContext.)
        response (js-await (js/fetch "/696317__mandaki__burmese-xylophone-sample.mp3"))
        array-buffer (js-await (.arrayBuffer response))
        audio-buffer (js-await (.decodeAudioData audio-context array-buffer))]
    {:audio {:context audio-context
             :buffer audio-buffer}}))

(defn ^:export playsound [game]
  (let [entity (ecs/get-single-component game :audio)
        context (:context entity)
        buffer (:buffer entity)
        source (.createBufferSource context)]
    (set! (.-buffer source) buffer)
    (.connect source (.-destination context))
    (.start source)))
