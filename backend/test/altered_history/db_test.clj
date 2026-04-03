(ns altered-history.db-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [next.jdbc :as jdbc]
            [altered-history.db :as db]))

(def ^:dynamic *ds* nil)

(defn with-test-db [f]
  (with-open [conn (jdbc/get-connection {:dbtype "sqlite" :dbname ":memory:"})]
    (db/run-migrations! conn)
    (binding [*ds* conn]
      (f))))

(use-fixtures :each with-test-db)

(defn- sample-game
  ([] (sample-game 123))
  ([table-id]
   {:table_id          table-id
    :player1_id        "p1"
    :player1_name      "Alice"
    :player1_hero      "Sigismar"
    :player1_faction   "Axiom"
    :player1_deck_name "Deck A"
    :player1_deck_id   "d1"
    :player2_id        "p2"
    :player2_name      "Bob"
    :player2_hero      "Fen"
    :player2_faction   "Bravos"
    :player2_deck_name "Deck B"
    :player2_deck_id   "d2"
    :winner_player_id  "p1"
    :played_at         1700000000}))

(deftest insert-game-test
  (testing "returns :inserted for a new game"
    (is (= :inserted (db/insert-game! *ds* (sample-game)))))

  (testing "returns :already-exists for a duplicate table_id"
    (db/insert-game! *ds* (sample-game 456))
    (is (= :already-exists (db/insert-game! *ds* (sample-game 456))))))

(deftest game-exists-test
  (testing "returns false when the table_id does not exist"
    (is (false? (db/game-exists? *ds* 999999))))

  (testing "returns true after insertion"
    (db/insert-game! *ds* (sample-game 100))
    (is (true? (db/game-exists? *ds* 100)))))

(deftest find-player-games-test
  (testing "returns empty for unknown player"
    (is (empty? (db/find-player-games *ds* "Nobody" 20 0))))

  (testing "finds games case-insensitively for player1 and player2"
    (db/insert-game! *ds* (assoc (sample-game 1) :played_at 1000))
    (is (= 1 (count (db/find-player-games *ds* "Alice" 20 0))))
    (is (= 1 (count (db/find-player-games *ds* "Bob" 20 0))))
    (is (= 1 (count (db/find-player-games *ds* "alice" 20 0))))
    (is (= 1 (count (db/find-player-games *ds* "ALICE" 20 0)))))

  (testing "returns total_count, ordered by played_at desc, with limit/offset"
    (db/insert-game! *ds* (assoc (sample-game 2) :played_at 2000))
    (db/insert-game! *ds* (assoc (sample-game 3) :played_at 3000))
    (let [games (db/find-player-games *ds* "Alice" 2 0)]
      (is (= 2 (count games)))
      (is (= 3 (:total_count (first games))))
      (is (= 3 (:table_id (first games)))))
    (let [games (db/find-player-games *ds* "Alice" 2 2)]
      (is (= 1 (count games)))
      (is (= 1 (:table_id (first games)))))))

(deftest migrations-test
  (testing "migrations are idempotent (running twice does not throw)"
    (db/run-migrations! *ds*)
    (is (= :inserted (db/insert-game! *ds* (sample-game 999))))))
