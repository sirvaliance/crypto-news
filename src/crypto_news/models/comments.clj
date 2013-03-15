(ns crypto-news.models.comments
  (:use monger.operators)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.conversion :as mcon]
            [monger.util :as mu]
            [noir.session :as session]
            [clj-time.core :as cltime]
            [monger.joda-time]
            [crypto-news.models.connection :as conn]
            [crypto-news.models.users :as users]))

(conn/db-connect)

(defn new-comment [post-id username text parent-id]
  (do
    (mc/update "posts" {:_id post-id} {$inc {:comments 1}})
    (let [id (mu/random-uuid)]
      (mc/insert "comments" {:_id id 
                            :post-id post-id
                            :parent-id parent-id
                            :text text 
                            :created (cltime/now)
                            :karma 0
                            :tags {}
                            :votes {} ; K: userid V: 1 or -1
                            :submitter username
                            :flag-counter 0})
        (str id))))

(defn get-comment-all [id]
  (do
    (mc/find-maps "comments" {:post-id id})))


(defn get-comment-by-id [id]
  (do 
    (mcon/from-db-object (mc/find-one "comments" {:_id id}) true)))

(defn upvote [id username]
  (do 
    (let [comment (mc/find-map-by-id "comments" id)]
      (if-not (or (.equals username (get comment :submitter)) (contains? (get comment :votes) (keyword username)))
        (do
          (mc/update "comments" {:_id id} {$set {:votes (assoc (get comment :votes) username 1)} $inc {:karma 1}})
          (users/change-comment-karma (get comment :submitter) 1))))))

(defn downvote [id username]
  (do 
    (let [comment (mc/find-map-by-id "comments" id)]
      (if-not (or (.equals username (get comment :submitter)) (contains? (get comment :votes) (keyword username)))
          (do
            (mc/update "comments" {:_id id} {$set {:votes (assoc (get comment :votes) username -1)} $inc {:karma -1}})
            (users/change-comment-karma (get comment :submitter) -1))))))

