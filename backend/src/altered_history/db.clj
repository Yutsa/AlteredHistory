(ns altered-history.db
  (:require [clojure.tools.logging :as log]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defonce ^:private ds-atom (atom nil))

(defn datasource []
  @ds-atom)

(defn- default-db-spec []
  {:dbtype "sqlite"
   :dbname (or (System/getenv "DATABASE_PATH") "data/altered_history.db")})

(def ^:private migrations
  ["migrations/001-create-games.sql"])

(defn run-migrations! [ds]
  (doseq [migration migrations]
    (log/info "Running migration:" migration)
    (let [sql (slurp (io/resource migration))]
      (jdbc/execute! ds [sql]))))

(defn- insert-or-ignore! [ds table row-map]
  (let [cols (keys row-map)
        col-names (str/join ", " (map name cols))
        placeholders (str/join ", " (repeat (count cols) "?"))
        sql (format "INSERT INTO %s (%s) VALUES (%s) ON CONFLICT DO NOTHING"
                    (name table) col-names placeholders)
        result (jdbc/execute-one! ds (into [sql] (map row-map cols)))]
    (if (= 1 (:next.jdbc/update-count result))
      :inserted
      :already-exists)))

(defn insert-game! [ds game-map]
  (insert-or-ignore! ds :games game-map))

(defn game-exists? [ds table-id]
  (some? (jdbc/execute-one! ds
           ["SELECT 1 FROM games WHERE table_id = ? LIMIT 1" table-id])))

(defn find-player-games [ds player-name limit offset]
  (let [name-lower (str/lower-case player-name)]
    (jdbc/execute! ds
      ["SELECT *, COUNT(*) OVER() AS total_count FROM games WHERE LOWER(player1_name) = ? OR LOWER(player2_name) = ? ORDER BY played_at DESC LIMIT ? OFFSET ?"
       name-lower name-lower limit offset]
      {:builder-fn rs/as-unqualified-maps})))

(defn init! []
  (let [db-spec (default-db-spec)
        _       (.mkdirs (io/file (.getParent (io/file (:dbname db-spec)))))
        ds      (jdbc/get-datasource db-spec)]
    (reset! ds-atom ds)
    (run-migrations! ds)
    (log/info "Database initialized:" (:dbname db-spec))))
