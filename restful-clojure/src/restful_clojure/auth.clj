(ns restful-clojure.auth
  (:use korma.core)
  (:require [restful-clojure.entities :as e]
            [restful-clojure.models.users :as users]
            [buddy.auth.backends.token :refer [token-backend]]
            [buddy.auth.accessrules :refer [success error]]
            [buddy.auth :refer [authenticated?]]
            [crypto.random :refer [base64]]))

(defn gen-session-id [] (base64 32))

(defn make-token!
  "Creates an auth token in the database for the given user and puts it in the database"
  [user-id]
  (let [token (gen-session-id)]
    (insert e/auth-tokens
      (values {:id token
               :user_id user-id}))
    token))

(defn authenticate-token
  "Validates a token, returning the id of the associated user when valid and nil otherwise"
  [req token]
  (let [sql (str "SELECT user_id "
                 "FROM auth_tokens "
                 "WHERE id = ? "
                 "AND created_at > current_timestamp - interval '6 hours'")]
    (some-> (exec-raw [sql [token]] :results)
            first
            :user_id
            users/find-by-id)))

(defn unauthorized-handler [req msg]
  {:status 401
   :body {:status :error
          :message (or msg "User not authorized")}})

;; Looks for an "Authorization" header with a value of "Token XXX"
;; where "XXX" is some valid token.
(def auth-backend (token-backend {:authfn authenticate-token
                                  :unauthorized-handler unauthorized-handler}))

;; Map of actions to the set of user types authorized to perform that action
(def permissions
  {"manage-lists"    #{:restful-clojure.models.users/user}
   "manage-products" #{:restful-clojure.models.users/admin}
   "manage-users"    #{:restful-clojure.models.users/admin}})

;;; Below are the handlers that Buddy will use for various authorization
;;; requirements the authenticated-user function determines whether a session
;;; token has been resolved to a valid user session, and the other functions
;;; take some argument and _return_ a handler that determines whether the
;;; user is authorized for some particular scenario. See handler.clj for usage.

(defn authenticated-user [req]
  (if (authenticated? req)
    true
    (error "User must be authenticated")))

;; Assumes that a check for authorization has already been performed
(defn user-can
  "Given a particular action that the authenticated user desires to perform,
  return a handler determining if their user level is authorized to perform
  that action."
  [action]
  (fn [req]
    (let [user-level (get-in req [:identity :level])
          required-levels (get permissions action #{})]
      (if (some #(isa? user-level %) required-levels)
        (success)
        (error (str "User of level " (name user-level) " is not authorized for action " (name action)))))))

(defn user-isa
  "Return a handler that determines whenther the authenticated user is of a
  specific level OR any derived level."
  [level]
  (fn [req]
    (if (isa? (get-in req [:identity :level]) level)
      (success)
      (error (str "User is not a(n) " (name level))))))

(defn user-has-id
  "Return a handler that determines whether the authenticated user has a given ID.
  This is useful, for example, to determine if the user is the owner of the requested
  resource."
  [id]
  (fn [req]
    (if (= id (get-in req [:identity :id]))
      (success)
      (error (str "User does not have id given")))))
