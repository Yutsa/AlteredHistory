(ns altered-history.handler
  (:require [clojure.tools.logging :as log]
            [reitit.ring :as ring]
            [ring.middleware.json :refer [wrap-json-response]]
            [altered-history.routes :as routes]))

(defn wrap-request-logging [handler]
  (fn [request]
    (let [response (handler request)]
      (log/info (str (:request-method request) " "
                     (:uri request) " -> "
                     (:status response)))
      response)))

(def app
  (-> (ring/ring-handler
        (ring/router routes/routes)
        (ring/create-default-handler
          {:not-found routes/not-found-handler}))
      wrap-request-logging
      wrap-json-response))
