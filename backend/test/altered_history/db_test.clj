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
  ([] (sample-game "123"))
  ([table-id]
   {:table_id          table-id
    :player1_id        "p1"
    :player1_hero      "Sigismar"
    :player1_faction   "Axiom"
    :player1_deck_name "Deck A"
    :player1_deck_id   "d1"
    :player2_id        "p2"
    :player2_hero      "Fen"
    :player2_faction   "Bravos"
    :player2_deck_name "Deck B"
    :player2_deck_id   "d2"
    :winner_player_id  "p1"}))

(deftest insert-game-test
  (testing "returns :inserted for a new game"
    (is (= :inserted (db/insert-game! *ds* (sample-game)))))

  (testing "returns :already-exists for a duplicate table_id"
    (db/insert-game! *ds* (sample-game "456"))
    (is (= :already-exists (db/insert-game! *ds* (sample-game "456"))))))

(deftest game-exists-test
  (testing "returns false when the table_id does not exist"
    (is (false? (db/game-exists? *ds* "nonexistent"))))

  (testing "returns true after insertion"
    (db/insert-game! *ds* (sample-game "100"))
    (is (true? (db/game-exists? *ds* "100")))))

(deftest migrations-test
  (testing "migrations are idempotent (running twice does not throw)"
    (db/run-migrations! *ds*)
    (is (= :inserted (db/insert-game! *ds* (sample-game "idempotent"))))))
