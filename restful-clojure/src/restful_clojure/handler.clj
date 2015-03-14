(ns restful-clojure.handler
  (:use compojure.core
        ring.middleware.json)
  (:import (com.fasterxml.jackson.core JsonGenerator))
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :refer [response]]
            [cheshire.generate :refer [add-encoder]]
            [restful-clojure.models.users :as users]
            [restful-clojure.models.lists :as lists]
            [restful-clojure.models.products :as products]
            [restful-clojure.auth :refer [auth-backend user-can user-isa user-has-id authenticated-user unauthorized-handler make-token!]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth.accessrules :refer [restrict]]))

; Strip namespace from namespaced-qualified kewywords, which is how wo represent user levels
(add-encoder clojure.lang.Keyword
  (fn [^clojure.lang.Keyword kw ^JsonGenerator gen]
    (.writeString gen (name kw))))

(defn get-users [_]
  {:status 200
   :body {:count (users/count-users)
          :results (users/find-all)}})

(defn create-user [{user :body}]
  (let [new-user (users/create user)]
    {:status 201
     :headers {"Location" (str "/users/" (:id new-user))}}))

(defn find-user [{{:keys [id]} :params}]
  (response (users/find-by-id (read-string id))))

(defn lists-for-user [{{:keys [id]} :params}]
  (response 
    (map #(dissoc % :user_id) (lists/find-all-by :user_id (read-string id)))))

(defn delete-user [{{:keys [id]} :params}]
  (users/delete-user {:id (read-string id)})
      {:status 204
       :headers {"Location" "/users"}})

(defn get-lists [_]
  {:status 200
   :body {:count (lists/count-lists)
          :results (lists/find-all)}})

(defn create-list [{listdata :body}]
  (let [new-list (lists/create listdata)]
     {:status 201
      :headers {"Location" (str "/users/" (:user_id new-list) "/lists")}}))

(defn find-list [{{:keys [id]} :params}]
  (response (lists/find-by-id (read-string id))))

(defn update-list [{{:keys [id]} :params
                    listdata :body}]
  (if (nil? id)
    {:status 404
     :headers {"Location" "/lists"}}

     ((lists/update-list (assoc listdata :id id))
     {:status 200
      :headers {"Location" (str "/lists/" id)}})))

(defn delete-list [{{:keys [id]} :params}]
  (lists/delete-list {:id (read-string id)})
  {:status 204
   :headers {"Location" "/lists"}})

(defn get-products [_]
  {:status 200
   :body {:count (products/count-products)
          :results (products/find-all)}})

(defn create-product [{product :body}]
  (let [new-prod (products/create product)]
     {:status 201
      :headers {"Location" (str "/products/" (:id new-prod))}}))

(defroutes app-routes
  ;; USERS
  (context "/users" []
    (GET "/" [] (-> get-users
                    (restrict {:handler {:and [authenticated-user (user-can "manage-users")]}
                               :on-error unauthorized-handler})))
    (POST "/" [] create-user)
    (context "/:id" [id]
      (restrict
        (routes
          (GET "/" [] find-user)
          (GET "/lists" [] lists-for-user))
        {:handler {:and [authenticated-user
                         {:or [(user-can "manage-users")
                               (user-has-id (read-string id))]}]}
         :on-error unauthorized-handler}))
    (DELETE "/:id" [id] (-> delete-user
                           (restrict {:handler {:and [authenticated-user (user-can "manage-users")]}
                                      :on-error unauthorized-handler}))))

  (POST "/sessions" {{:keys [user-id password]} :body}
    (if (users/password-matches? user-id password)
      {:status 201
       :body {:auth-token (make-token! user-id)}}
      {:status 409
       :body {:status "error"
              :message "invalid username or password"}}))

  ;; LISTS
  (context "/lists" []
    (GET "/" [] (-> get-lists
                    (restrict {:handler {:and [authenticated-user (user-isa :restful-clojure.models.users/admin)]}
                               :on-error unauthorized-handler})))
    (POST "/" [] (-> create-list
                     (restrict {:handler {:and [authenticated-user (user-can "manage-lists")]}
                                :on-error unauthorized-handler})))

    (context "/:id" [id]
      (let [owner-id (get (lists/find-by-id (read-string id)) :user_id)]
        (restrict
          (routes
            (GET "/" [] find-list)
            (PUT "/" [] update-list)
            (DELETE "/" [] delete-list))
          {:handler {:and [authenticated-user
                           {:or [(user-can "manage-lists")
                                 (user-has-id owner-id)]}]}
           :on-error unauthorized-handler}))))

  ;; PRODUCTS
  (context "/products" []
    (restrict
      (routes
        (GET "/" [] get-products)
        (POST "/" [] create-product))
      {:handler {:and [authenticated-user (user-can "manage-products")]}
       :on-error unauthorized-handler}))

  (route/not-found (response {:message "Page not found"})))

(defn wrap-log-request [handler]
  (fn [req]
    (println req)
    (handler req)))

(def app
  (-> app-routes
      (wrap-authentication auth-backend)
      (wrap-authorization auth-backend)
      wrap-json-response
      (wrap-json-body {:keywords? true})))
