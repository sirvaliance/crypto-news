(ns crypto-news.models.posts
  (:use monger.operators
        clojure.contrib.math)
  (:refer-clojure :exclude [sort find])
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.conversion :as mcon]
            [monger.util :as mu]
            [monger.query :as mq]
            [noir.session :as session]
            [clj-time.core :as cltime]
            [monger.joda-time]
            [crypto-news.models.connection :as conn]
            [crypto-news.models.users :as users]
            [crypto-news.views.utils :as utils]
            [clojurewerkz.urly.core :as urly]))

(conn/db-connect)

(defn find-post-by-url [url]
  (do
    (let [post-map (mcon/from-db-object (mc/find-one "posts" {:url url}) true)]
      (if-not (empty? post-map)
        (post-map)))))

(defn new-post [title url post-text username]
  (do
    (let [id (mu/random-uuid)]
      (mc/insert "posts" {:_id id 
                          :title title 
                          :url (if (.equals "" url) (str "/post/" id "/") url)
                          :url-host (if-not (.equals "" url) (urly/host-of (urly/url-like url)) "")
                          :text post-text 
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
        (do
          (mc/update "posts" {:_id id} {$set {:votes (assoc (get post :votes) username 1)} $inc {:karma 1}})
          (users/change-submission-karma (get post :submitter) 1))))))


(defn downvote [id username]
  (do 
    (let [post (mc/find-map-by-id "posts" id)]
      (if-not (or (.equals username (get post :submitter)) (contains? (get post :votes) (keyword username)))
        (do
          (mc/update "posts" {:_id id} {$set {:votes (assoc (get post :votes) username -1)} $inc {:karma -1}})
          (users/change-submission-karma (get post :submitter) -1)
          )))))


(defn get-posts-by-date []
  (mq/with-collection "posts"
                      (mq/find {})
                      (mq/sort {:created -1})))

;; Most algos use time in hours, not minutes.
;; Test and tweak
(defn compute-gravity [karma post-time]
  (/ karma (expt (+ (/ (utils/time-diff post-time) 60.0) 2.0) 1.8)))

(defn post-gravity [post]
  (let [compu-karma (compute-gravity 
                      (get post :karma)
                      (get post :created))]
    (assoc post :compu-karma compu-karma)))

(defn get-post-with-algo []
  (let [posts (mc/find-maps "posts")]
    (sort-by :compu-karma > (map post-gravity posts))))
