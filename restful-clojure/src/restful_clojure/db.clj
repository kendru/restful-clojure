(ns restful-clojure.db
  (:use korma.db)
  (:require [environ.core :refer [env]]))

(defdb db (postgres {:db (get env :restful-db "restful_clojure")
                     :user (get env :restful-db-user "restful_clojure")
                     :password (get env :restful-db-pass "")
                     :host (get env :restful-db-host "localhost")
                     :port (get env :restful-db-port 5432)}))
