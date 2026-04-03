(ns altered-history.replay-parser
  (:require [clojure.tools.logging :as log]))

(defn- flatten-events
  "Flattens data.logs packets into a seq of event maps,
   each carrying its parent packet's :channel."
  [logs]
  (mapcat (fn [packet]
            (let [channel (:channel packet)]
              (map (fn [event]
                     (assoc event :channel channel))
                   (:data packet))))
          logs))

(defn- extract-channel-player-id
  "Extracts player ID string from a channel like '/player/p95656346'."
  [channel]
  (when-let [[_ pid] (re-find #"/player/p(\d+)" channel)]
    pid))

(defn- extract-table-id [logs]
  (let [tid (:table_id (first logs))]
    (when-not tid
      (throw (ex-info "No table_id found in logs"
                      {:type :invalid-replay})))
    (long tid)))

(defn- extract-players [setup-events]
  (when-not (= 2 (count setup-events))
    (throw (ex-info (str "Expected 2 setupPlayer events, found " (count setup-events))
                    {:type :missing-events :event-type "setupPlayer"
                     :count (count setup-events)})))
  (mapv (fn [event]
          (let [args (:args event)]
            {:player-id   (str (:player_id args))
             :player-name (str (:player_name args))
             :hero        (get-in args [:card :properties :name])
             :faction     (get-in args [:card :properties :faction])}))
        setup-events))

(defn- extract-deck-selections [table-id deck-events]
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
      (do (log/info "Preconstructed deck detected, skipping replay" {:table-id table-id})
          nil))))

(defn- extract-winner [table-id state-changes]
  (let [with-result   (filter #(get-in % [:args :args :result]) state-changes)
        last-with-res (last with-result)]
    (when-not last-with-res
      (throw (ex-info "No gameStateChange with result found"
                      {:type :missing-events :event-type "gameStateChange"})))
    (let [result (get-in last-with-res [:args :args :result])]
      (if (every? :tie result)
        (do (log/info "Tie detected, skipping replay" {:table-id table-id})
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

(defn- build-game-map [table-id [player1 player2] deck-selections winner-pid]
  (let [deck1 (get deck-selections (:player-id player1))
        deck2 (get deck-selections (:player-id player2))]
    {:table_id          table-id
     :player1_id        (:player-id player1)
     :player1_name      (:player-name player1)
     :player1_hero      (:hero player1)
     :player1_faction   (:faction player1)
     :player1_deck_name (:deck-name deck1)
     :player1_deck_id   (:deck-id deck1)
     :player2_id        (:player-id player2)
     :player2_name      (:player-name player2)
     :player2_hero      (:hero player2)
     :player2_faction   (:faction player2)
     :player2_deck_name (:deck-name deck2)
     :player2_deck_id   (:deck-id deck2)
     :winner_player_id  winner-pid}))

(defn parse-replay
  "Processes a parsed BGA replay map and returns a map compatible with insert-game!,
   or nil if the replay should be skipped (precon deck, tie)."
  [parsed]
  (let [logs            (validate-replay-structure parsed)
        events          (flatten-events logs)
        events-by-type  (group-by :type events)
        table-id        (extract-table-id logs)
        players         (extract-players (get events-by-type "setupPlayer"))
        deck-selections (extract-deck-selections table-id (get events-by-type "updateInitialPrecoDeckSelection"))]
    (when deck-selections
      (validate-consistency players deck-selections)
      (when-let [winner-pid (extract-winner table-id (get events-by-type "gameStateChange"))]
        (build-game-map table-id players deck-selections winner-pid)))))
