(ns crypto-news.views.index
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

(defn render-index-post [post-map & post-num]
  (html5
    [:div.news-item.row-fluid
      [:div.span12
        [:div.news-item-vote
            [:span.news-num (if (seq post-num) (str (first post-num)))]
          (if (users/logged-in?)
            (html5
              [:a.arrow-up {:href (str "/post/" (get post-map :_id) "/vote/up/")} "&#x25B2;"]
              [:a.arrow-down {:href (str "/post/" (get post-map :_id) "/vote/down/")} "&#x25BC;"]
              ))

        ]
        [:div.news-item-link
          [:a {:href (get post-map :url)} (get post-map :title)]
          [:span "&nbsp;"]
          [:span (str "(" (get post-map :url-host) ")")]]
        [:div.news-item-info
          [:span (str (get post-map :karma) " Points")]
          [:span "by"]
          [:span
           [:a {:href (str "/user/" (get post-map :submitter) "/")} (get post-map :submitter)]]
          [:span (string-date-formater (get post-map :created))]
          [:span
           [:a {:href (str "/post/" (get post-map :_id) "/")} (str (get post-map :comments) " Comments")]]]]]))

(defn index []
  (layout
    (let [posts-list (posts/get-post-all)]
     ; Should contain obvious ranking algorithm
     ; http://amix.dk/blog/post/19574
      (for [i (range (count posts-list))]
        (render-index-post (nth posts-list i) (+ i 1))))))

