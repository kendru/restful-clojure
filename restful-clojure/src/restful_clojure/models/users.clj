(ns restful-clojure.models.users
  (:use korma.core)
  (:require [restful-clojure.entities :as e]))

(defn find-all []
  (select e/users))

(defn find-by [field value]
  (first
    (select e/users
      (where {field value})
      (limit 1))))

(defn find-by-id [id]
  (find-by :id id))

(defn for-list [listdata]
  (find-by-id (listdata :user_id)))

(defn find-by-email [email]
  (find-by :email email))

(defn create [user]
  (insert e/users
    (values user)))

(defn update-user [user]
  (update e/users
    (set-fields (dissoc user :id))
    (where {:id (user :id)})))

(defn count-users []
  (let [agg (select e/users
              (aggregate (count :*) :cnt))]
    (get-in agg [0 :cnt] 0)))

(defn delete-user [user]
  (delete e/users
    (where {:id (user :id)})))