(ns crypto-news.handler
  (:use compojure.core
        crypto-news.views
        crypto-news.settings
        ring.middleware.session.cookie
        ring.middleware.cookies
        noir.session)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.cookies :as cook]))

(defroutes app-routes
  (GET "/" [] (index))
  (GET "/login/" [] (login-get))
  (POST "/login/" [input-username input-password] (login-post input-username input-password))
  (GET "/logout/" [] (logout-get))
  (GET "/signup/" [] (signup-get))
  (POST "/signup/" [input-username input-password input-password-confirm]
       (signup-post input-username input-password input-password-confirm))
  (GET "/user/:username" [username] (user-get username))
  (GET "/post/new/" [] (new-post-get))
  (POST "/post/new/" [title url text] (new-post-post title url text))
  (GET "/post/:id/" [id] (get-post-get id))
  (GET "/post/:id/vote/up/" [id] (post-upvote id))
  (GET "/comment/:id/" [id] (comment-get id))
  (POST "/comment/:id/" [parent-id text post-id] (post-comment parent-id text post-id))
  (GET "/comment/:id/vote/up/" [id] (comment-upvote id))
  (route/files "/" {:root "resources/public"})
  (route/not-found "Not Found"))

(def app
  (->
    app-routes
    handler/site
    cook/wrap-noir-cookies
    (wrap-noir-session {:store (cookie-store {:key session-key})})))
