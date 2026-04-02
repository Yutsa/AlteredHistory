(ns altered-history.routes
  (:require [altered-history.replay :as replay]
            [altered-history.db :as db]))

(defn health-handler [_request]
  {:status 200
   :body   {:status "ok"}})

(defn post-replay-handler [request]
  (let [body (:body request)]
    (if-not (map? body)
      {:status 400
       :body   {:error "Empty or non-JSON body"}}
      (try
        (let [result (replay/process-replay! (db/datasource) body)]
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

(defn not-found-handler [_request]
  {:status 404
   :body   {:error "Not found"}})

(def routes
  [["/api/health"  {:get  {:handler health-handler}}]
   ["/api/replays" {:post {:handler post-replay-handler}}]])
