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
            [noir.cookies :as cook]
            [noir.util.crypt :as crypt]
            [clj-time.core :as cltime]
            [clj-time.coerce :as t-coerce]
            [crypto-news.models.users :as users]
            [crypto-news.models.posts :as posts]
            [crypto-news.models.comments :as comnt]))


(defn login-get []
  (layout [:h1 "Login to Crypto News"]
            [:form.form-horizontal
             {:method "POST" :action "/login/"}
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
               [:label.checkbox "Remember Me"
                [:input {:type "checkbox"}]]
               [:button.btn {:type "submit"} "Sign In"]]]]))

(defn login-post [username password]
   (let [user (users/get-user username)]
     (if (and user (.equals (get user :username) username))
       (if (crypt/compare password (get user :password))
         (do
           (session/put! :user (get user :username))
           (session/put! :login-time (str (t-coerce/to-string (cltime/now))))
           (cook/put-signed! (str cookie-key (session/get :login-time)) :li "true")
           (resp/redirect "/"))
         (resp/redirect "/login/"))

       ; Should send to a function where error messages can be passed and rendered
       ; This one would be "Username or Passord Invalid"
       (resp/redirect "/login/"))))


(defn logout-get []
   (users/log-out!)
   (resp/redirect "/"))


(defn signup-get []
  (layout [:h1 "Signup For Crypto News"]
            [:form.form-horizontal
             {:method "POST" :action "/signup/"}
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
               [:button.btn {:type "submit"} "Sign In"]]]]))


(defn signup-post [username password password-confirm]
  (do 
    ; Needs to check if user exists
     (if (= password password-confirm)
       (let [hashpass (crypt/encrypt password-salt password)]
         (users/new-user {:username (escape-html username)
                          :password hashpass})
         (layout 
           [:h1 "Signup Successful"]
           [:a {:href "/login/"} "Login"]))
       (do
         (println "ERROR")
         (layout 
           [:h1 "SIGNUP ERROR"])))))

