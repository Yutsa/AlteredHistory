(ns altered-history.test-helpers
  (:require [cheshire.core :as json]))

(defn load-replay [filename]
  (json/parse-string (slurp (str "../doc/" filename)) true))
