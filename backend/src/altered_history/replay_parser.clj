(ns altered-history.replay-parser
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log]))

(defn- flatten-events
  "Flattens data.logs packets into a seq of event maps,
   each carrying its parent packet's :channel and :table_id."
  [logs]
  (mapcat (fn [packet]
            (let [channel  (:channel packet)
                  table-id (:table_id packet)]
              (map (fn [event]
                     (assoc event :channel channel :table_id table-id))
                   (:data packet))))
          logs))

(defn- extract-channel-player-id
  "Extracts player ID from a channel string like '/player/p95656346'."
  [channel]
  (when-let [[_ pid] (re-find #"/player/p(\d+)" channel)]
    pid))

(defn- extract-table-id [events]
  (let [tid (:table_id (first events))]
    (when-not tid
      (throw (ex-info "No table_id found in events"
                      {:type :invalid-replay})))
    (str tid)))

(defn- extract-players [events]
  (let [setup-events (filter #(= "setupPlayer" (:type %)) events)]
    (when-not (= 2 (count setup-events))
      (throw (ex-info (str "Expected 2 setupPlayer events, found " (count setup-events))
                      {:type :missing-events :event-type "setupPlayer"
                       :count (count setup-events)})))
    (mapv (fn [event]
            (let [args (:args event)]
              {:player-id (str (:player_id args))
               :hero      (get-in args [:card :properties :name])
               :faction   (get-in args [:card :properties :faction])}))
          setup-events)))

(defn- extract-deck-selections [events]
  (let [deck-events (filter #(= "updateInitialPrecoDeckSelection" (:type %)) events)]
    (when-not (= 2 (count deck-events))
      (throw (ex-info (str "Expected 2 updateInitialPrecoDeckSelection events, found "
                           (count deck-events))
                      {:type :missing-events
                       :event-type "updateInitialPrecoDeckSelection"
                       :count (count deck-events)})))
    (let [selections (map (fn [event]
                            (let [player-id (extract-channel-player-id (:channel event))
                                  private   (get-in event [:args :args :_private])
                                  selection (:selection private)]
                              (when-not player-id
                                (throw (ex-info "Cannot extract player ID from channel"
                                                {:type :invalid-replay
                                                 :channel (:channel event)})))
                              {:player-id player-id
                               :selection selection
                               :api       (:API private)}))
                          deck-events)]
      (if (every? #(= "API" (:selection %)) selections)
        (into {} (map (fn [{:keys [player-id api]}]
                        [player-id {:deck-name (:deckName api)
                                    :deck-id   (:id api)
                                    :faction   (:faction api)}])
                      selections))
        (do (log/info "Preconstructed deck detected, skipping replay")
            nil)))))

(defn- extract-winner [events]
  (let [state-changes (filter #(= "gameStateChange" (:type %)) events)
        with-result   (filter #(get-in % [:args :args :result]) state-changes)
        last-with-res (last with-result)]
    (when-not last-with-res
      (throw (ex-info "No gameStateChange with result found"
                      {:type :missing-events :event-type "gameStateChange"})))
    (let [result (get-in last-with-res [:args :args :result])]
      (if (every? :tie result)
        (do (log/info "Tie detected, skipping replay")
            nil)
        (let [winner (first (filter #(and (= "1" (:score %)) (= 1 (:rank %))) result))]
          (when-not winner
            (throw (ex-info "Cannot determine winner from result"
                            {:type :invalid-replay :result result})))
          (str (:player winner)))))))

(defn- validate-consistency [players deck-selections]
  (let [player-ids      (set (map :player-id players))
        deck-player-ids (set (keys deck-selections))]
    (when-not (= player-ids deck-player-ids)
      (throw (ex-info "Player IDs mismatch between setupPlayer and deck selection channels"
                      {:type           :inconsistent-data
                       :setup-players  player-ids
                       :deck-channels  deck-player-ids})))
    (doseq [{:keys [player-id faction]} players]
      (let [deck-faction (get-in deck-selections [player-id :faction])]
        (when-not (= faction deck-faction)
          (throw (ex-info (str "Faction mismatch for player " player-id
                               ": setupPlayer=" faction ", deck=" deck-faction)
                          {:type       :inconsistent-data
                           :player-id  player-id
                           :setup-faction faction
                           :deck-faction  deck-faction})))))))

(defn- validate-replay-structure [parsed]
  (when-not (= 1 (:status parsed))
    (throw (ex-info (str "Unexpected replay status: " (:status parsed))
                    {:type :invalid-replay :status (:status parsed)})))
  (let [logs (get-in parsed [:data :logs])]
    (when-not logs
      (throw (ex-info "Missing data.logs in replay"
                      {:type :invalid-replay})))
    logs))

(defn- build-game-map [table-id [p1 p2] deck-selections winner-pid]
  (let [d1 (get deck-selections (:player-id p1))
        d2 (get deck-selections (:player-id p2))]
    {:table_id          table-id
     :player1_id        (:player-id p1)
     :player1_hero      (:hero p1)
     :player1_faction   (:faction p1)
     :player1_deck_name (:deck-name d1)
     :player1_deck_id   (:deck-id d1)
     :player2_id        (:player-id p2)
     :player2_hero      (:hero p2)
     :player2_faction   (:faction p2)
     :player2_deck_name (:deck-name d2)
     :player2_deck_id   (:deck-id d2)
     :winner_player_id  winner-pid}))

(defn parse-replay
  "Parses a BGA replay JSON string and returns a map compatible with insert-game!,
   or nil if the replay should be skipped (precon deck, tie)."
  [replay-json-string]
  (let [parsed          (json/parse-string replay-json-string true)
        logs            (validate-replay-structure parsed)
        events          (flatten-events logs)
        table-id        (extract-table-id events)
        players         (extract-players events)
        deck-selections (extract-deck-selections events)]
    (when deck-selections
      (validate-consistency players deck-selections)
      (when-let [winner-pid (extract-winner events)]
        (build-game-map table-id players deck-selections winner-pid)))))
