(ns restful-clojure.users-test
  (:use clojure.test
        restful-clojure.test-core)
  (:require [restful-clojure.models.users :as users]
            [environ.core :refer [env]]))

; Run each test in an isolated db transaction and rollback
; afterwards
(use-fixtures :each with-rollback)

(deftest create-read-users
  (testing "Create user"
    (let [count-orig (users/count-users)]
      (users/create {:name "Charlie" :email "charlie@example.com"})
      (is (= (inc count-orig) (users/count-users)))))
  
  (testing "Retrieve user"
    (let [user (users/create {:name "Andrew" :email "me@mytest.com"})
          found-user (users/find-by-id (user :id))]
      (is (= "Andrew" (found-user :name))
      (is (= "me@mytest.com" (found-user :email))))))

  (testing "Find by email"
    (users/create {:name "John Doe" :email "j.doe@ihearttractors.com"})
    (let [user (users/find-by-email "j.doe@ihearttractors.com")]
      (is (= "John Doe" (user :name))))))

(deftest multiple-user-operations
  (testing "Find all users"
    (doseq [i (range 10)]
      (users/create {:name "Test user"
                     :email (str "user." i "@example.com")}))
    (is (= 10 (count (users/find-all))))))

(deftest update-users
  (testing "Modifies existing user"
    (let [user-orig (users/create {:name "Curious George" :email "i.go.bananas@hotmail.com"})
          user-id (user-orig :id)]
      (users/update-user (assoc user-orig :name "Chiquita Banana"))
      (is (= "Chiquita Banana" (:name (users/find-by-id user-id)))))))

(deftest delete-users
  (testing "Decreases user count"
    (let [user (users/create {:name "Temporary" :email "ephemerial@shortlived.org"})
          user-count (users/count-users)]
      (users/delete-user user)
      (is (= (dec user-count) (users/count-users)))))
  
  (testing "Deleted correct user"
    (let [user-keep (users/create {:name "Keep" :email "important@users.net"})
          user-del (users/create {:name "Delete" :email "irrelevant@users.net"})]
      (users/delete-user user-del)
      (is (= user-keep
             (users/find-by-id (user-keep :id))))
      (is (= nil
             (users/find-by-id (user-del :id)))))))
