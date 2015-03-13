(ns restful-clojure.models.lists
  (:use korma.core)
  (:require [restful-clojure.entities :as e]
            [clojure.set :refer [difference]]))

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

(defn remove-product [listdata product-id]
  (delete "lists_products"
    (where {:list_id (:id listdata)
            :product_id product-id}))
   (update-in listdata [:products]
     (fn [products] (remove #(= (:id %) product-id) products))))

(defn- get-product-ids-for
  "Gets a set of all product ids that belong to a particular list"
  [listdata]
  (into #{}
    (map :product_id
      (select "lists_products"
        (fields :product_id)
        (where {:list_id (:id listdata)})))))

(defn update-list [listdata]
  (update e/lists
    (set-fields (dissoc listdata :id :products))
    (where {:id (:id listdata)}))
  (let [existing-product-ids (get-product-ids-for listdata)
        updated-product-ids (->> (:products listdata)
                                 (map :id)
                                 (into #{}))
        to-add (difference updated-product-ids existing-product-ids)
        to-remove (difference existing-product-ids updated-product-ids)]
    (doseq [prod-id to-add]
      (add-product listdata prod-id))
    (doseq [prod-id to-remove]
      (remove-product listdata prod-id))
    (find-by-id (:id listdata))))

(defn delete-list [listdata]
  (delete e/lists
    (where {:id (:id listdata)})))