(ns crypto-news.models.users
  (:use crypto-news.settings)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [noir.session :as session]
            [noir.cookies :as cook]
            [noir.response :as nr]
            [clj-time.core :as cltime]
            [crypto-news.models.connection :as conn]))

(conn/db-connect)

(defn logged-in? []
  (let [li (cook/get-signed 
             (str cookie-key (session/get :login-time))
             :li)]
    (if-not (nil? li)
      (if (.equals "true" li)
        true
        false)
      false)))

(defn get-username []
  (if (logged-in?)
    (session/get :user)
    (str "")))

(defn log-out! []
  (do
    (session/clear!)
    (cook/put-signed!
             (str cookie-key (session/get :login-time))
             :li 
             "false")
    (nr/redirect "/")))

(defn new-user [user-map]
  (do
    (mc/insert "user" {:username (get user-map :username)
                       :password (get user-map :password)
                       :created (cltime/now)
                       :karma-comment 0
                       :karma-submission 0
                       :profile ""
                       :email ""
                       :gpg-pubkey ""
                       :comments []
                       :submissions []})))

(defn get-user [username]
  (do 
    (let [user-map (mc/find-maps "user" {:username username})]
      (if (= (count user-map) 1)
        (first user-map)))))
