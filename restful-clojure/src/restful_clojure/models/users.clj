(ns restful-clojure.models.users
  (:use korma.core)
  (:require [restful-clojure.entities :as e]
            [buddy.hashers :as hashers]
            [clojure.set :refer [map-invert]]))

(def user-levels
  {"user" ::user
   "admin" ::admin})
(derive ::admin ::user)

(defn- with-kw-level [user]
  (assoc user :level
              (get user-levels (:level user) ::user)))

(defn- with-str-level [user]
  (assoc user :level (if-let [level (:level user)]
                       (name level)
                       "user")))

(defn find-all []
  (select e/users))

(defn find-by [field value]
  (some-> (select* e/users)
          (where {field value})
          (limit 1)
          select
          first
          (dissoc :password_digest)
          with-kw-level))

(defn find-by-id [id]
  (find-by :id id))

(defn for-list [listdata]
  (find-by-id (listdata :user_id)))

(defn find-by-email [email]
  (find-by :email email))

(defn create [user]
  (-> (insert* e/users)
      (values (-> user
                  (assoc :password_digest (hashers/encrypt (:password user)))
                  with-str-level
                  (dissoc :password)))
      insert
      (dissoc :password_digest)
      with-kw-level))

(defn update-user [user]
  (update e/users
    (set-fields (-> user
                    (dissoc :id :password)
                    with-str-level))
    (where {:id (user :id)})))

(defn count-users []
  (let [agg (select e/users
              (aggregate (count :*) :cnt))]
    (get-in agg [0 :cnt] 0)))

(defn delete-user [user]
  (delete e/users
    (where {:id (user :id)})))

(defn password-matches?
  "Check to see if the password given matches the digest of the user's saved password"
  [id password]
  (some-> (select* e/users)
            (fields :password_digest)
            (where {:id id})
            select
            first
            :password_digest
            (->> (hashers/check password))))