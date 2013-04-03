(ns crypto-news.views.auth
  (:use hiccup.core 
        hiccup.page
        hiccup.util
        compojure.response
        crypto-news.views.common
        crypto-news.views.utils
        crypto-news.settings)
  (:require [noir.response :as resp]
            [noir.session :as session]
            [noir.util.crypt :as crypt]
            [clj-time.core :as cltime]
            [clj-time.coerce :as t-coerce]
            [crypto-news.models.users :as users]
            [crypto-news.models.posts :as posts]
            [crypto-news.models.comments :as comnt]))


(defn login-get [& errors]
  (layout 
    (if-not (nil? errors)
      [:div.errors
       [:span.label.label-important.error-large errors]])
    [:form.form-horizontal {:action "/login/" :method "POST"}
     [:fieldset
      [:legend "Login to Cypherpunk News"]
      [:div.control-group
       [:label.control-label {:for "input-username"} "Username"]
       [:div.controls
        [:input {:name "input-username" :type "text" :id "input-username" :placeholder "Username"}]]]
      [:div.control-group
       [:label.control-label {:for "input-passord"} "Password"]
       [:div.controls
        [:input {:name "input-password" :type "password" :id "input-password" :placeholder "Password"}]]]
      [:div.control-group
       [:div.controls
        [:button.btn {:type "submit"} "Sign In"]]]]]))

(defn login-post [username password]
  (let [user (users/get-user username)]
    (if (and user (.equals (get user :username) username))
      (if (crypt/compare password (get user :password))
        (do
          (session/put! :user (get user :username))
          (session/put! :login-time (str (t-coerce/to-string (cltime/now))))
          (session/put! :li "true")
          (resp/redirect "/"))
        ;(resp/redirect "/login/"))
        (login-get "Bad Login Information"))

      ; Should send to a function where error messages can be passed and rendered
      ; This one would be "Username or Passord Invalid"
      (login-get "Bad Login Information"))))


(defn logout-get []
  (users/log-out!)
  (resp/redirect "/"))


(defn signup-get [& errors]
  (layout 
    (if-not (nil? errors)
      [:div.errors
       [:span.label.label-important.error-large errors]])

    [:form.form-horizontal
     {:method "POST" :action "/signup/"}
     [:fieldset
      [:legend "Signup For Crypto News"]
      [:div.control-group
       [:label.control-label {:for "input-username"} "Username"]
       [:div.controls
        [:input {:name "input-username" :type "text" :id "input-username" :placeholder "Username"}]]]
      [:div.control-group
       [:label.control-label {:for "input-password"} "Password"]
       [:div.controls
        [:input {:name "input-password" :type "password" :id "input-password" :placeholder "Password"}]]]
      [:div.control-group
       [:label.control-label {:for "input-password-confirm"} "Password Confirm"]
       [:div.controls
        [:input {:name "input-password-confirm" :type "password" :id "input-password-confirm" :placeholder "Password Confirm"}]]]
      [:div.control-group
       [:div.controls
        [:button.btn {:type "submit"} "Sign Up"]]]]]))


(defn signup-post [username password password-confirm]
  ; Needs to check if user exists
  (let [username-check (users/get-user username)]
    (if (= (count username-check) 0)

      (if (= password password-confirm)
        (let [hashpass (crypt/encrypt password-salt password)]
          (do
            (users/new-user {:username (escape-html username)
                             :password hashpass})
            (resp/redirect "/login/")))
        (signup-get "Passwords do not match"))
      (signup-get "Username exists"))))

