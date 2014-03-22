(ns restful-clojure.lists-test
  (:use clojure.test
        restful-clojure.test-core)
  (:require [restful-clojure.models.lists :as lists]
            [restful-clojure.models.products :as products]
            [restful-clojure.models.users :as users]))

(use-fixtures :each with-rollback)

(deftest create-list
  (let [user (users/create {:name "Test user" :email "me@mytest.com"})]

    (testing "Create a list for given user"
      (let [count-orig (lists/count-lists)]
        (lists/create {:user_id (:id user) :title "New Test"})
        (is (= (inc count-orig) (lists/count-lists)))))))

(deftest retrieve-list
  (let [user (users/create {:name "Test user" :email "me@mytest.com"})]

    (testing "Retrieve a list by id"
      (let [my-list (lists/create {:user_id (:id user) :title "My list"})
            found-list (lists/find-by-id (:id my-list))]
        (is (= "My list" (:title found-list)))
        (is (= (:id user) (:user_id found-list)))))))

(deftest update-list
  (let [user (users/create {:name "Test user" :email "me@mytest.com"})]

    (testing "Modifies existing list"
      (let [list-orig (lists/create {:user_id (:id user)
                                     :title "FP Languages"})
            list-id (:id list-orig)]
        (lists/update-list (assoc list-orig :title "Awesome Languages"))
        (is (= "Awesome Languages" (:title (lists/find-by-id list-id))))))))

(deftest delete-lists
  (let [user (users/create {:name "Test user" :email "me@mytest.com"})
        user-id (:id user)]
    (testing "Decreases list count"
      (let [listdata (lists/create {:user_id user-id :title "Unimportant things"})
            list-count (lists/count-lists)]
        (lists/delete-list listdata)
        (is (= (dec list-count) (lists/count-lists)))))

    (testing "Deleted correct list"
      (let [list-keep (lists/create {:user_id user-id :title "Stuff to keep"})
            list-del (lists/create {:user_id user-id :title "Stuff to delete"})]
        (lists/delete-list list-del)
        (is (seq
               (lists/find-by-id (:id list-keep))))
        (is (nil?
               (lists/find-by-id (:id list-del))))))))

(deftest add-products
  (let [user (users/create {:name "Test user" :email "me@mytest.com"})
        my-list (lists/create {:user_id (:id user) :title "My list"})
        pintos (products/create {:title "Pinto Beans"
                                 :description "Yummy beans for burritos"})]
    (testing "Adds product to existing list"
      (let [modified-list (lists/add-product my-list (:id pintos))]
        (is (= [pintos] (:products modified-list)))))

    (testing "Creates new list with products"
      (let [listdata (lists/create {:user_id (:id user)
                                    :title "Most interesting"
                                    :products [pintos]})]
        (is (= [pintos] (:products listdata)))
        (is (= [pintos] (:products (lists/find-by-id (:id listdata)))))))))
