(ns restful-clojure.handler-test
  (:use clojure.test
        restful-clojure.test-core
        ring.mock.request  
        restful-clojure.handler)
  (:require [cheshire.core :as json]
            [restful-clojure.models.users :as u]
            [restful-clojure.models.lists :as l]
            [restful-clojure.models.products :as p]))

(use-fixtures :each with-rollback)

(deftest main-routes
  (testing "list users"
    (let [response (app (request :get "/users"))]
      (is (= (:status response) 200))
      (is (= (get-in response [:headers "Content-Type"]) "application/json; charset=utf-8"))))
  
  (testing "lists endpoint"
    (let [response (app (request :get "/lists"))]
      (is (= (:status response) 200))
      (is (= (get-in response [:headers "Content-Type"]) "application/json; charset=utf-8"))))

  (testing "products endpoint"
    (let [response (app (request :get "/products"))]
      (is (= (:status response) 200))
      (is (= (get-in response [:headers "Content-Type"]) "application/json; charset=utf-8"))))

  (testing "not-found route"
    (let [response (app (request :get "/bogus-route"))]
      (is (= (:status response) 404)))))

(deftest create-user
  (testing "POST /users"
    (let [user-count (u/count-users)
          response (app (-> (request :post "/users")
                            (body "{\"name\":\"Joe Test\",\"email\":\"joe@example.com\"}")
                            (content-type "application/json")
                            (header "Accept" "application/json")))]
      (is (= (:status response) 201))
      (is (substring? "/users/" (get-in response [:headers "Location"])))
      (is (= (inc user-count) (u/count-users))))))

(deftest retrieve-user-stuff
  (let [user (u/create {:name "John Doe" :email "j.doe@mytest.com"})]

    (testing "GET /users"
      (doseq [i (range 4)]
        (u/create {:name "Person" :email (str "person" i "@example.com")}))
      (let [response (app (request :get "/users"))
            resp-data (json/parse-string (:body response))]
        (is (= (:status response 200)))
        ; A person's email contained in the response body
        (is (substring? "person3@example.com" (:body response)))
        ; All results present (including the user created in the let form)
        (is (= 5 (count (get resp-data "results" []))))
        ; "count" field present
        (is (= 5 (get resp-data "count" [])))))

    (testing "GET /users/:id"
      (let [response (app (request :get (str "/users/" (:id user))))]
        (is (= (:body response) (json/generate-string user)))))

    (testing "GET /users/:id/lists"
      (let [my-list (l/create {:user_id (:id user) :title "Wonderful Stuffs"})
            response (app (request :get (str "/users/" (:id user) "/lists")))]
        (is (= (:body response) (json/generate-string [(dissoc my-list :user_id)])))))))

(deftest delete-user
  (let [user (u/create {:name "John Doe" :email "j.doe@mytest.com"})]

    (testing "DELETE /users/:id"
      (let [response (app (request :delete (str "/users/" (:id user))))]
        ; okay/no content status
        (is (= (:status response) 204))
        ; redirected to users index
        (is (= "/users" (get-in response [:headers "Location"])))
        ; user no longer exists in db
        (is (nil? (u/find-by-id (:id user))))))))

(deftest create-list
  (testing "POST /lists"
    (let [list-count (l/count-lists)
          user (u/create {:name "John Doe" :email "j.doe@mytest.com"})
          response (app (-> (request :post "/lists")
                            (body (str "{\"user_id\":" (:id user) ",\"title\":\"Amazing Accoutrements\"}"))
                            (content-type "application/json")
                            (header "Accept" "application/json")))]
      (is (= (:status response) 201))
      (is (substring? "/users/" (get-in response [:headers "Location"])))
      (is (= (inc list-count) (l/count-lists))))))

(deftest retrieve-list
  (let [user (u/create {:name "John Doe" :email "j.doe@mytest.com"})
        listdata (l/create {:user_id (:id user) :title "Root Beers of Iowa"})]

    (testing "GET /lists"
      (doseq [i (range 4)]
        (l/create {:user_id (:id user) :title (str "List " i)}))
      (let [response (app (request :get "/lists"))
            resp-data (json/parse-string (:body response))]
        (is (= (:status response 200)))
        ; A list title
        (is (substring? "List 3" (:body response)))
        ; All results present
        (is (= 5 (count (get resp-data "results" []))))
        ; "count" field present
        (is (= 5 (get resp-data "count" [])))))

    (testing "GET /lists/:id"
      (let [response (app (request :get (str "/lists/" (:id listdata))))]
        (is (= (:body response) (json/generate-string listdata)))))))

(deftest delete-list
  (let [user (u/create {:name "John Doe" :email "j.doe@mytest.com"})
        listdata (l/create {:user_id (:id user) :title "Root Beers of Iowa"})]

    (testing "DELETE /lists/:id"
      (let [response (app (request :delete (str "/lists/" (:id listdata))))]
        ; okay/no content status
        (is (= (:status response) 204))
        ; redirected to users index
        (is (= "/lists" (get-in response [:headers "Location"])))
        ; list no longer exists in db
        (is (nil? (l/find-by-id (:id listdata))))))))

(deftest create-product
  (testing "POST /products"
    (let [prod-count (p/count-products)
          response (app (-> (request :post "/products")
                            (body (str "{\"title\":\"Granny Smith\",\"description\":\"Howdya like them apples?\"}"))
                            (content-type "application/json")
                            (header "Accept" "application/json")))]
      (is (= (:status response) 201))
      (is (substring? "/products/" (get-in response [:headers "Location"])))
      (is (= (inc prod-count) (p/count-products))))))

(deftest retrieve-product
  (testing "GET /products"
    (doseq [i (range 5)]
      (p/create {:title (str "Product " i)}))
    (let [response (app (request :get "/products"))
          resp-data (json/parse-string (:body response))]
      (is (= (:status response 200)))
      ; Product name contained in the response body
      (is (substring? "Product 4" (:body response)))
      ; All results present
      (is (= 5 (count (get resp-data "results" []))))
      ; "count" field present
      (is (= 5 (get resp-data "count" []))))))

