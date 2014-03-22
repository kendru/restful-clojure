(ns restful-clojure.handler
  (:use compojure.core
        ring.middleware.json)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :refer [response]]
            [restful-clojure.models.users :as users]
            [restful-clojure.models.lists :as lists]
            [restful-clojure.models.products :as products]))

(defroutes app-routes
  ;; USERS
  (context "/users" []
    (GET "/" []
      {:status 200
       :body {:count (users/count-users)
              :results (users/find-all)}})
    (POST "/" {user :body}
      (let [new-user (users/create user)]
        {:status 201
         :headers {"Location" (str "/users/" (:id new-user))}}))
    (GET "/:id" [id]
      (response (users/find-by-id (read-string id))))
    (GET "/:id/lists" [id] (response 
      (map #(dissoc % :user_id) (lists/find-all-by :user_id (read-string id)))))
    (DELETE "/:id" [id]
      (users/delete-user {:id (read-string id)})
      {:status 204
       :headers {"Location" "/users"}}))

  ;; LISTS
  (context "/lists" []
    (GET "/" []
      {:status 200
       :body {:count (lists/count-lists)
              :results (lists/find-all)}})
    (POST "/" {listdata :body}
      (let [new-list (lists/create listdata)]
       {:status 201
        :headers {"Location" (str "/users/" (:user_id new-list) "/lists")}}))
    (GET "/:id" [id]
      (response (lists/find-by-id (read-string id))))
    (PUT "/:id" {params :params listdata :body}
      (let [id (:id params)]
        (if (nil? id)
          {:status 404
           :headers {"Location" "/lists"}}
           
           ((lists/update-list (assoc listdata :id id))
           {:status 200
            :headers {"Location" (str "/lists/" id)}}))))
      (response (lists/find-by-id (read-string id))))
    (DELETE "/:id" [id]
      (lists/delete-list {:id (read-string id)})
      {:status 204
       :headers {"Location" "/lists"}}))

  ;; PRODUCTS
  (context "/products" []
    (GET "/" []
      {:status 200
       :body {:count (products/count-products)
              :results (products/find-all)}})
    (POST "/" {product :body}
      (let [new-prod (products/create product)]
       {:status 201
        :headers {"Location" (str "/products/" (:id new-prod))}})))

  (route/not-found (response {:message "Page not found"})))

(defn wrap-log-request [handler]
  (fn [req]
    (println req)
    (handler req)))

(def app
  (-> app-routes
    wrap-json-response
    wrap-json-body))
