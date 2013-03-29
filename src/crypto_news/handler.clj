(ns crypto-news.handler
  (:use compojure.core
        crypto-news.settings
        ring.middleware.session.cookie
        ring.middleware.cookies)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.cookies :as cook]
            [noir.response :as resp]
            [noir.session :as sesh]
            [crypto-news.views.index :as views-inx]
            [crypto-news.views.posts :as views-pst]
            [crypto-news.views.users :as views-usr]
            [crypto-news.views.auth :as views-ath]
            [crypto-news.models.users :as users]))

(defroutes app-routes
           (GET "/" [] (views-inx/index))
           (GET "/new/" [] (views-inx/new-posts))
           (GET "/login/" [] (views-ath/login-get))
           (POST "/login/" [input-username input-password] (views-ath/login-post 
                                                             input-username 
                                                             input-password))
           (GET "/logout/" [] (views-ath/logout-get))
           (GET "/signup/" [] (views-ath/signup-get))
           (POST "/signup/" [input-username input-password input-password-confirm]
                 (views-ath/signup-post 
                   input-username 
                   input-password 
                   input-password-confirm))
           (GET "/user/:username/" [username] (views-usr/user-get 
                                                username))
           (POST "/user/:username/" [username email profile gpg-pubkey] (views-usr/user-update 
                                                                          username
                                                                          email 
                                                                          profile 
                                                                          gpg-pubkey))
           (GET "/post/new/" [] (views-pst/new-post-get))
           (POST "/post/new/" [title url post-text] (views-pst/new-post-post title url post-text))
           (GET "/post/:id/" [id] (views-pst/get-post-get id))
           (GET "/post/:id/vote/up/" [id] (views-pst/post-upvote id))
           (GET "/post/:id/vote/down/" [id] (views-pst/post-downvote id))

           (GET "/comment/:id/" [id] (views-pst/comment-get id))
           (POST "/comment/:id/" [parent-id text post-id] (views-pst/post-comment parent-id text post-id))
           (GET "/comment/:id/vote/up/" [id] (views-pst/comment-upvote id))
           (GET "/comment/:id/vote/down/" [id] (views-pst/comment-downvote id))

           (route/files "/" {:root "resources/public"})
           (route/not-found "Not Found"))

(def app
  (->
    app-routes
    handler/site
    cook/wrap-noir-cookies
    (sesh/wrap-noir-session {:store (cookie-store {:key session-key})
                             :cookie-attrs {:max-age (* 60 60 60 24 7)}})))
