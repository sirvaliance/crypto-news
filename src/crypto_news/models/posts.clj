(ns crypto-news.models.posts
 (:use monger.operators)
 (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.conversion :as mcon]
            [monger.util :as mu]
            [noir.session :as session]
            [clj-time.core :as cltime]
            [monger.joda-time]
            [crypto-news.models.connection :as conn]
            [clojurewerkz.urly.core :as urly]))

(conn/db-connect)

(defn find-post-by-url [url]
  (do
    (let [post-map (mcon/from-db-object (mc/find-one "posts" {:url url}) true)]
      (if-not (empty? post-map)
        (post-map)))))

(defn new-post [title url text username]
  (do
    (let [id (mu/random-uuid)]
      (mc/insert "posts" {:_id id 
                          :title title 
                          :url (if (.equals "" url) (str "/post/" id "/") url)
                          :url-host (if-not (.equals "" url) (urly/host-of (urly/url-like url)) "")
                          :text text 
                          :created (cltime/now)
                          :karma 0
                          :comments 0
                          :tags {}
                          :votes {} ; K: userid V: -1 or 1
                          :submitter username
                          :flag-counter 0})
      (str id))))


(defn get-post-by-id [id]
  (do
    (mcon/from-db-object (mc/find-one "posts" {:_id id}) true)))

(defn get-post-all []
  (do
    (mc/find-maps "posts")))

(defn upvote [id username]
  (do 
    (let [post (mc/find-map-by-id "posts" id)]
      (if-not (or (.equals username (get post :submitter)) (contains? (get post :votes) (keyword username)))
          (mc/update "posts" {:_id id} {$set {:votes (assoc (get post :votes) username 1)} $inc {:karma 1}})))))

(defn downvote [id username]
  (do 
    (let [post (mc/find-map-by-id "posts" id)]
      (if-not (or (.equals username (get post :submitter)) (contains? (get post :votes) (keyword username)))
          (mc/update "posts" {:_id id} {$set {:votes (assoc (get post :votes) username -1)} $inc {:karma -1}})))))

