(ns altered-history.core
  (:require [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]]
            [altered-history.handler :refer [app]]))

(def port 8080)

(defn -main [& _args]
  (log/info (str "Starting server on port " port))
  (run-jetty app {:port port :join? true}))
