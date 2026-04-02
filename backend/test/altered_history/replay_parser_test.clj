(ns altered-history.replay-parser-test
  (:require [clojure.test :refer [deftest is testing]]
            [altered-history.replay-parser :as parser]
            [altered-history.test-helpers :refer [load-replay]]))

(deftest parse-normal-game-test
  (testing "extracts all fields from a normal game replay"
    (let [result (parser/parse-replay (load-replay "sample_replay1.json"))]
      (is (some? result))
      (is (= 829783480 (:table_id result)))
      (is (= "95656346" (:player1_id result)))
      (is (= "Della & Bolt" (:player1_hero result)))
      (is (= "AX" (:player1_faction result)))
      (is (= "DellaS5" (:player1_deck_name result)))
      (is (= "01KN2JCM5XNZVPTXBGQV7Z2GWN" (:player1_deck_id result)))
      (is (= "91926087" (:player2_id result)))
      (is (= "Akesha & Taru" (:player2_hero result)))
      (is (= "YZ" (:player2_faction result)))
      (is (= "Akesha Burst 4" (:player2_deck_name result)))
      (is (= "01KKZV8Q0J0B49DS6JJ3QSCG4F" (:player2_deck_id result)))
      (is (= "91926087" (:winner_player_id result)))))

  (testing "table_id is an integer, other values are strings"
    (let [result (parser/parse-replay (load-replay "sample_replay1.json"))]
      (is (integer? (:table_id result)))
      (doseq [[k v] (dissoc result :table_id)]
        (is (string? v) (str k " should be a string"))))))

(deftest parse-concede-game-test
  (testing "correctly determines winner when opponent concedes"
    (let [result (parser/parse-replay (load-replay "sample_replay2.json"))]
      (is (some? result))
      (is (= 829600896 (:table_id result)))
      (is (= "91926087" (:player1_id result)))
      (is (= "Akesha & Taru" (:player1_hero result)))
      (is (= "YZ" (:player1_faction result)))
      (is (= "98451398" (:player2_id result)))
      (is (= "Basira & Kaizaimon" (:player2_hero result)))
      (is (= "BR" (:player2_faction result)))
      (is (= "98451398" (:winner_player_id result))))))

(deftest parse-precon-deck-test
  (testing "returns nil for games with preconstructed decks"
    (is (nil? (parser/parse-replay (load-replay "sample_replay_precon.json"))))))

(deftest parse-invalid-input-test
  (testing "throws on missing data.logs"
    (is (thrown? clojure.lang.ExceptionInfo
          (parser/parse-replay {:status 1 :data {}}))))

  (testing "throws on unexpected status"
    (is (thrown? clojure.lang.ExceptionInfo
          (parser/parse-replay {:status 0 :data {:logs []}})))))
