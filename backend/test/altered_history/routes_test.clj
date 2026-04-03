(ns altered-history.routes-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.string :as str]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            [next.jdbc :as jdbc]
            [altered-history.handler :refer [app]]
            [altered-history.db :as db]
            [altered-history.test-helpers :refer [load-replay]]))

(defn- parse-json-body [response]
  (json/parse-string (:body response) true))

(defn- with-test-db [f]
  (with-open [conn (jdbc/get-connection {:dbtype "sqlite" :dbname ":memory:"})]
    (db/run-migrations! conn)
    (with-redefs [db/datasource (constantly conn)]
      (f))))

(use-fixtures :each with-test-db)

(defn- post-replay [body]
  (app (-> (mock/request :post "/api/replays")
           (mock/content-type "application/json")
           (mock/body (json/generate-string {:replay body :start 1700000000})))))

(deftest health-endpoint-test
  (testing "GET /api/health returns 200 with status ok"
    (let [response (app (mock/request :get "/api/health"))]
      (is (= 200 (:status response)))
      (is (= {:status "ok"} (parse-json-body response)))
      (is (str/includes? (get-in response [:headers "Content-Type"] "") "application/json")))))

(deftest not-found-test
  (testing "GET unknown route returns 404 with JSON error"
    (let [response (app (mock/request :get "/unknown-route"))]
      (is (= 404 (:status response)))
      (is (= {:error "Not found"} (parse-json-body response)))
      (is (str/includes? (get-in response [:headers "Content-Type"] "") "application/json")))))

(deftest post-replay-test
  (testing "returns 201 created for a valid replay"
    (let [replay   (load-replay "sample_replay1.json")
          response (post-replay replay)]
      (is (= 201 (:status response)))
      (let [body (parse-json-body response)]
        (is (= "created" (:status body)))
        (is (= 829783480 (:table_id body))))))

  (testing "returns 200 already_exists for a duplicate replay"
    (let [replay (load-replay "sample_replay1.json")
          _first (post-replay replay)
          response (post-replay replay)]
      (is (= 200 (:status response)))
      (is (= "already_exists" (:status (parse-json-body response))))))

  (testing "returns 200 skipped for a preconstructed deck replay"
    (let [replay   (load-replay "sample_replay_precon.json")
          response (post-replay replay)]
      (is (= 200 (:status response)))
      (is (= "skipped" (:status (parse-json-body response))))))

  (testing "returns 400 for an invalid replay"
    (let [response (post-replay {:status 1 :data {}})]
      (is (= 400 (:status response)))
      (is (some? (:error (parse-json-body response))))))

  (testing "returns 400 when start is missing"
    (let [response (app (-> (mock/request :post "/api/replays")
                            (mock/content-type "application/json")
                            (mock/body (json/generate-string {:replay (load-replay "sample_replay1.json")}))))]
      (is (= 400 (:status response)))
      (is (= "Missing required fields: replay, start" (:error (parse-json-body response))))))

  (testing "returns 400 for empty body"
    (let [response (app (mock/request :post "/api/replays"))]
      (is (= 400 (:status response)))))

  (testing "returns 400 for non-JSON body"
    (let [response (app (-> (mock/request :post "/api/replays")
                            (mock/content-type "text/plain")
                            (mock/body "not json")))]
      (is (= 400 (:status response))))))

(deftest get-player-games-test
  (testing "returns 404 for unknown player"
    (let [response (app (mock/request :get "/api/players/Nobody/games"))]
      (is (= 404 (:status response)))
      (is (some? (:error (parse-json-body response))))))

  (testing "returns games for a known player"
    (post-replay (load-replay "sample_replay1.json"))
    (let [response (app (mock/request :get "/api/players/Yutsa/games"))
          body     (parse-json-body response)]
      (is (= 200 (:status response)))
      (is (= 1 (:total_count body)))
      (is (= 1 (count (:games body))))
      (let [game (first (:games body))]
        (is (= 829783480 (:table_id game)))
        (is (true? (:won game)))
        (is (= "Gibari13" (get-in game [:opponent :name]))))))

  (testing "case-insensitive search"
    (let [response (app (mock/request :get "/api/players/yutsa/games"))]
      (is (= 200 (:status response)))
      (is (= 1 (:total_count (parse-json-body response))))))

  (testing "pagination parameters"
    (post-replay (load-replay "sample_replay2.json"))
    (let [response (app (mock/request :get "/api/players/Yutsa/games?page=1&page_size=1"))
          body     (parse-json-body response)]
      (is (= 200 (:status response)))
      (is (= 1 (:page body)))
      (is (= 1 (:page_size body)))
      (is (= 2 (:total_count body)))
      (is (= 2 (:total_pages body)))
      (is (= 1 (count (:games body))))))

  (testing "player as player1 and player2 both included"
    (let [response (app (mock/request :get "/api/players/Yutsa/games"))
          body     (parse-json-body response)]
      (is (= 2 (:total_count body))))))
