(ns altered-history.replay-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [next.jdbc :as jdbc]
            [altered-history.replay :as replay]
            [altered-history.db :as db]
            [altered-history.test-helpers :refer [load-replay]]))

(defn- with-test-db [f]
  (with-open [conn (jdbc/get-connection {:dbtype "sqlite" :dbname ":memory:"})]
    (db/run-migrations! conn)
    (f conn)))

(use-fixtures :each (fn [f]
                      (with-test-db (fn [conn] (with-redefs [db/datasource (constantly conn)]
                                                 (f))))))

(deftest process-replay-test
  (testing "returns :created for a valid replay"
    (let [result (replay/process-replay! (db/datasource) (load-replay "sample_replay1.json"))]
      (is (= :inserted (:status result)))
      (is (= 829783480 (:table_id result)))))

  (testing "returns :already-exists for a duplicate replay"
    (let [replay (load-replay "sample_replay1.json")
          _first (replay/process-replay! (db/datasource) replay)
          result (replay/process-replay! (db/datasource) replay)]
      (is (= :already-exists (:status result)))
      (is (= 829783480 (:table_id result)))))

  (testing "returns :skipped for a preconstructed deck replay"
    (let [result (replay/process-replay! (db/datasource) (load-replay "sample_replay_precon.json"))]
      (is (= :skipped (:status result)))
      (is (nil? (:table_id result)))))

  (testing "throws ExceptionInfo for an invalid replay"
    (is (thrown? clojure.lang.ExceptionInfo
                (replay/process-replay! (db/datasource) {:status 1 :data {}})))))
