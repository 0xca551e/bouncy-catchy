(ns spawner
  (:require ["three" :as three]
            [ball :as ball]))

(defn ^:export assemble [location velocity]
  {:spawner {:location location :velocity velocity}})

(defn ^:export spawn [game spawner-entity]
  (let [ball (ball/assemble game (-> spawner-entity :spawner :location) (-> spawner-entity :spawner :velocity))]
    (.add (:world game) ball)))
