(ns altered-history.core
  (:require [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]]
            [altered-history.handler :refer [app]]
            [altered-history.db :as db]))

(def port 8080)

(defn -main [& _args]
  (log/info "Starting server on port" port)
  (db/init!)
  (run-jetty app {:port port :join? true}))
