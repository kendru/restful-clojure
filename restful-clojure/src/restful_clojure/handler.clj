(ns restful-clojure.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]))

(defn- str-to [num]
  (apply str (interpose ", " (range 1 (inc num)))))

(defn- str-from [num]
  (apply str (interpose ", " (reverse (range 1 (inc num))))))

(defroutes app
  (GET "/count-up/:to" [to] (str-to (Integer. to)))
  (GET "/count-down/:from" [from] (str-from (Integer. from))))

