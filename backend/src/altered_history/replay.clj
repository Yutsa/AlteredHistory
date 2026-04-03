(ns altered-history.replay
  (:require [altered-history.replay-parser :as parser]
            [altered-history.db :as db]))

(defn process-replay!
  "Parses a replay and inserts the resulting game into the database.
   played-at is the Unix timestamp of the game start.
   Returns a result map with :status (:skipped, :created, :already-exists)
   and optionally :table_id."
  [datasource replay-data played-at]
  (if-let [result (parser/parse-replay replay-data)]
    (let [game (assoc result :played_at played-at)]
      {:status   (db/insert-game! datasource game)
       :table_id (:table_id game)})
    {:status :skipped}))
