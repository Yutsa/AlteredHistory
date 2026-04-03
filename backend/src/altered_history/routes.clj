(ns altered-history.routes
  (:require [altered-history.replay :as replay]
            [altered-history.db :as db]
            [clojure.string :as str]))

(defn health-handler [_request]
  {:status 200
   :body   {:status "ok"}})

(defn post-replay-handler [request]
  (let [body        (:body request)
        replay-data (:replay body)
        start       (:start body)]
    (cond
      (not (map? body))
      {:status 400
       :body   {:error "Empty or non-JSON body"}}

      (or (nil? replay-data) (nil? start))
      {:status 400
       :body   {:error "Missing required fields: replay, start"}}

      :else
      (try
        (let [result (replay/process-replay! (db/datasource) replay-data (long start))]
          (case (:status result)
            :skipped        {:status 200
                             :body   {:status "skipped"}}
            :inserted       {:status 201
                             :body   {:table_id (:table_id result)
                                      :status   "created"}}
            :already-exists {:status 200
                             :body   {:table_id (:table_id result)
                                      :status   "already_exists"}}))
        (catch clojure.lang.ExceptionInfo e
          {:status 400
           :body   {:error   (.getMessage e)
                    :details (ex-data e)}})))))

(defn- parse-positive-long [s default]
  (or (some-> s parse-long (max 1)) default))

(defn- orient-game [game player-name]
  (let [is-player1 (= (str/lower-case player-name) (str/lower-case (:player1_name game)))
        [self opponent] (if is-player1 ["player1" "player2"] ["player2" "player1"])
        field          (fn [prefix col] (get game (keyword (str prefix "_" col))))
        details        (fn [prefix] {:hero      (field prefix "hero")
                                     :faction   (field prefix "faction")
                                     :deck_name (field prefix "deck_name")})]
    {:table_id         (:table_id game)
     :played_at        (:played_at game)
     :opponent         {:id   (field opponent "id")
                        :name (field opponent "name")}
     :player           (details self)
     :opponent_details (details opponent)
     :won              (= (:winner_player_id game) (field self "id"))}))

(defn get-player-games-handler [request]
  (let [player-name (get-in request [:path-params :name])
        params      (:query-params request)
        page        (parse-positive-long (get params "page") 1)
        page-size   (min 100 (parse-positive-long (get params "page_size") 20))
        offset      (* (dec page) page-size)
        rows        (db/find-player-games (db/datasource) player-name page-size offset)]
    (if (empty? rows)
      {:status 404
       :body   {:error (str "No games found for player: " player-name)}}
      (let [total-count (:total_count (first rows))
            total-pages (int (Math/ceil (/ (double total-count) page-size)))]
        {:status 200
         :body   {:page        page
                  :page_size   page-size
                  :total_count total-count
                  :total_pages total-pages
                  :games       (mapv #(orient-game % player-name) rows)}}))))

(defn not-found-handler [_request]
  {:status 404
   :body   {:error "Not found"}})

(def routes
  [["/api/health"              {:get  {:handler health-handler}}]
   ["/api/replays"             {:post {:handler post-replay-handler}}]
   ["/api/players/:name/games" {:get  {:handler get-player-games-handler}}]])
