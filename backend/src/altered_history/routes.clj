(ns altered-history.routes)

(defn health-handler [_request]
  {:status 200
   :body   {:status "ok"}})

(defn not-found-handler [_request]
  {:status 404
   :body   {:error "Not found"}})

(def routes
  [["/api/health" {:get {:handler health-handler}}]])
