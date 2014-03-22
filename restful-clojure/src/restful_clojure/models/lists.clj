(ns restful-clojure.models.lists
  (:use korma.core)
  (:require [restful-clojure.entities :as e]))

(declare add-product)

(defn find-all []
  (select e/lists
    (with e/products)))

(defn find-by [field value]
  (first
    (select e/lists
      (with e/products)
      (where {field value})
      (limit 1))))

(defn find-all-by [field value]
  (select e/lists
    (with e/products)
    (where {field value})))

(defn find-by-id [id]
  (find-by :id id))

(defn for-user [userdata]
  (find-all-by :user_id (:id userdata)))

(defn count-lists []
  (let [agg (select e/lists
              (aggregate (count :*) :cnt))]
    (get-in agg [0 :cnt] 0)))

(defn create [listdata]
  (let [newlist (insert e/lists
                  (values (dissoc listdata :products)))]
    (doseq [product (:products listdata)]
      (add-product newlist (:id product) "incomplete"))
    (assoc newlist :products (into [] (:products listdata)))))

(defn add-product
  "Add a product to a list with an optional status arg"
  ([listdata product-id]
    (add-product listdata product-id "incomplete"))
  ([listdata product-id status]
    (let [sql (str "INSERT INTO lists_products ("
                   "list_id, product_id, status"
                   ") VALUES ("
                   "?, ?, ?::item_status"
                   ")")]
      (exec-raw [sql [(:id listdata) product-id status] :results])
      (find-by-id (:id listdata)))))

(defn update-list [listdata]
  (update e/lists
    (set-fields (dissoc listdata :id :products))
    (where {:id (:id listdata)})))

(defn delete-list [listdata]
  (delete e/lists
    (where {:id (:id listdata)})))