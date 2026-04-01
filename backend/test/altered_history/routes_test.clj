(ns altered-history.routes-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            [altered-history.handler :refer [app]]))

(defn- parse-json-body [response]
  (json/parse-string (:body response) true))

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
