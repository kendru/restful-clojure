(ns restful-clojure.users-test
  (:use clojure.test
        restful-clojure.test-core)
  (:require [restful-clojure.models.users :as users]
            [restful-clojure.entities :as e]
            [korma.core :as sql]
            [environ.core :refer [env]]))

; Run each test in an isolated db transaction and rollback
; afterwards
(use-fixtures :each with-rollback)

(deftest create-read-users
  (testing "Create user"
    (let [count-orig (users/count-users)]
      (users/create {:name "Charlie" :email "charlie@example.com" :password "foo"})
      (is (= (inc count-orig) (users/count-users)))))
  
  (testing "Retrieve user"
    (let [user (users/create {:name "Andrew" :email "me@mytest.com" :password "foo"})
          found-user (users/find-by-id (user :id))]
      (is (= "Andrew" (found-user :name))
      (is (= "me@mytest.com" (found-user :email))))))

  (testing "Find by email"
    (users/create {:name "John Doe" :email "j.doe@ihearttractors.com" :password "foo"})
    (let [user (users/find-by-email "j.doe@ihearttractors.com")]
      (is (= "John Doe" (user :name)))))

  (testing "Create with user level"
    (let [admin (users/create {:name "Jane Doe" :email "jane@ihearttractors.com" :password "foo" :level "admin"})
          expected-level :restful-clojure.models.users/admin]
      (is (= expected-level (:level admin)))
      (is (= expected-level (:level (users/find-by-id (:id admin))))))))

(deftest multiple-user-operations
  (testing "Find all users"
    (doseq [i (range 10)]
      (users/create {:name "Test user"
                     :email (str "user." i "@example.com")
                     :password "foo"}))
    (is (= 10 (count (users/find-all))))))

(deftest update-users
  (testing "Modifies existing user"
    (let [user-orig (users/create {:name "Curious George" :email "i.go.bananas@hotmail.com" :password "foo"})
          user-id (user-orig :id)]
      (users/update-user (assoc user-orig :name "Chiquita Banana"))
      (is (= "Chiquita Banana" (:name (users/find-by-id user-id)))))))

(deftest delete-users
  (testing "Decreases user count"
    (let [user (users/create {:name "Temporary" :email "ephemerial@shortlived.org" :password "foo"})
          user-count (users/count-users)]
      (users/delete-user user)
      (is (= (dec user-count) (users/count-users)))))
  
  (testing "Deleted correct user"
    (let [user-keep (users/create {:name "Keep" :email "important@users.net" :password "foo"})
          user-del (users/create {:name "Delete" :email "irrelevant@users.net" :password "foo"})]
      (users/delete-user user-del)
      (is (= (dissoc user-keep :password)
             (users/find-by-id (user-keep :id))))
      (is (nil?
             (users/find-by-id (user-del :id)))))))

(deftest authenticate-users
  (let [user (users/create {:name "Sly" :email "sly@falilystone.com" :password "s3cr3t"})
        user-id (:id user)]
    (testing "Accepts the correct password"
      (is (users/password-matches? user-id "s3cr3t")))
    
    (testing "Rejects incorrect passwords"
      (is (not (users/password-matches? user-id "not_my_password"))))
    
    (testing "Does not store the password in plain text"
      (let [stored-pass (:password_digest (first (sql/select e/users
                                                    (sql/where {:email "sly@falilystone.com"}))))]
        (is (not= stored-pass "s3cr3t"))))

    (testing "Does not include password or digest on user response"
      (let [user (users/find-by-id user-id)]
        (is (nil? (:password user)))
        (is (nil? (:password_digest user)))))))
