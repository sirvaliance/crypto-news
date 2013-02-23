(ns crypto-news.models.users
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [noir.session :as session]
            [clj-time.core :as cltime]
            [crypto-news.models.connection :as conn]))

(conn/db-connect)

(defn logged-in? []
  (session/get :user))


(defn log-out! []
  (session/clear!)) 

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
