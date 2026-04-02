(ns altered-history.replay
  (:require [altered-history.replay-parser :as parser]
            [altered-history.db :as db]))

(defn process-replay!
  "Parses a replay and inserts the resulting game into the database.
   Returns a result map with :status (:skipped, :created, :already-exists)
   and optionally :table_id."
  [datasource replay-data]
  (if-let [result (parser/parse-replay replay-data)]
    {:status   (db/insert-game! datasource result)
     :table_id (:table_id result)}
    {:status :skipped}))
