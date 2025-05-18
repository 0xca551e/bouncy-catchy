(ns spawner
  (:require ["three" :as three]
            [ball :as ball]))

(defn ^:export assemble [location velocity]
  {:spawner {:location location :velocity velocity}})

(defn ^:export spawn [game spawner-entity color]
  (let [b (ball/assemble game (-> spawner-entity :spawner :location) (-> spawner-entity :spawner :velocity) color)]
    (.add (:world game) b)
    b))
