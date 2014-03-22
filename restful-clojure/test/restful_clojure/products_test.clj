(ns restful-clojure.products-test
  (:use clojure.test
        restful-clojure.test-core)
  (:require [restful-clojure.models.products :as products]))

(use-fixtures :each with-rollback)

(deftest create-product
  (testing "Create a product increments product count"
    (let [count-orig (products/count-products)]
      (products/create {:title "Cherry Tomatos"
                        :description "Tasty red tomatos"})
      (is (= (inc count-orig) (products/count-products))))))