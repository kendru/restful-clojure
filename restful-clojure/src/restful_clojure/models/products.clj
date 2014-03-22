(ns restful-clojure.models.products
  (:use korma.core)
  (:require [restful-clojure.entities :as e]))

(defn create [product]
  (insert e/products
    (values product)))

(defn find-all []
  (select e/products))

(defn find-by [field value]
  (first
    (select e/products
      (where {field value})
      (limit 1))))

(defn find-all-by [field value]
  (select e/products
    (where {field value})))

(defn find-by-id [id]
  (find-by :id id))

(defn count-products []
  (let [agg (select e/products
              (aggregate (count :*) :cnt))]
    (get-in agg [0 :cnt] 0)))

(defn update-product [product]
  (update e/products
    (set-fields (dissoc product :id))
    (where {:id (product :id)})))

(defn delete-product [product]
  (delete e/products
    (where {:id (product :id)})))