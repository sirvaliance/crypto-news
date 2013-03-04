(ns crypto-news.views.posts
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
            [crypto-news.views.index :as views-idx]
            [crypto-news.models.users :as users]
            [crypto-news.models.posts :as posts]
            [crypto-news.models.comments :as comnt]))



(defn new-post-get[]
  (if-not (users/logged-in?)
    (resp/redirect "/login/")

    (layout
      [:form.form-horizontal {:method "POST" :action "/post/new/"}
       [:fieldset
         [:legend "Submit a New Post"]
         [:div.control-group
           [:label.control-label {:for "title"} "Title"]
           [:div.controls
            [:input.span6 {:name "title" :type "text" :id "title" :placeholder "Title"}]]]
         [:div.control-group
           [:label.control-label {:for "url"} "Url"]
           [:div.controls
            [:input.span6 {:name "url" :type "text" :id "url" :placeholder "Url"}]]]
         [:div.control-group
           [:label.control-label {:for "text"} "Self Post"]
           [:div.controls
            [:textarea.span6 {:name "text" :id "text" :cols "80" :rows "10" :placeholder "Enter text here for a self post"}]]]
          [:div.control-group
            [:div.controls
               [:button.btn {:type "submit"} "Submit"]]]]])))


(defn new-post-post [title url text]
  (if (users/logged-in?)
    (if (posts/find-post-by-url url)
      ; Throw error saying it has been used
      (resp/redirect "/post/new/")
      (do 
        ;Make sure the user is logged in
        (let [id (posts/new-post (escape-html title) (escape-html url) (escape-html text) (users/get-username))]
          (resp/redirect (str "/post/" id "/")))))
    (resp/redirect "/login/")))
  ; Check if url has already been used, if so, redirect to that submission
  ; If no url, check for text.  If no text and no url, reload the /post/new/ page
  ; If a text post, validate for proper formatting (markdown) and insert into db
  ; If a url post, validate url and post to db

(defn filter-level [root-id comments]
  (filter #(.equals root-id (get % :parent-id)) comments))

(defn remove-level [root-id comments]
  (remove #(.equals root-id (get % :parent-id)) comments))

(defn build-comment-tree [level-id current-level]
    (let [comm-seq (filter-level level-id current-level)]
      (for [comment-head comm-seq]
       (if-not (= 0 (count comm-seq))
        [:div.comment-block
         [:div.comment
           [:div.comment-head
              [:span
                   (if (users/logged-in?)
                     [:a.comment-arrow {:href (str "/comment/" (get comment-head :_id) "/vote/up/")} "&#x25B2;"])
                   [:span (str "&nbsp;" (get comment-head :karma) "&nbsp;Points&nbsp;")]
                   [:a {:href (str "/user/" (get comment-head :submitter) "/")} (get comment-head :submitter)]
                   [:span "&nbsp;"]
                   [:span (string-date-formater (get comment-head :created))]
                   [:span "&nbsp;|&nbsp;"]
                   [:a {:href (str "/comment/" (get comment-head :_id) "/")} "Link/Reply"]]]
         [:div.comment-body
          [:span (get comment-head :text)]]]
              (build-comment-tree (get comment-head :_id) (remove-level level-id current-level))
       ]))))


(defn get-post-get [id]
  (let [post-map (posts/get-post-by-id id)
        comments (comnt/get-comment-all id)]
    (layout
      (views-idx/render-index-post post-map)
      (if (users/logged-in?)
        (html5
          [:form {:method "POST" :action (str "/comment/" id "/")}
             [:input {:type "hidden" :name "parent-id" :value id}]
             [:input {:type "hidden" :name "post-id" :value id}]
             [:div.control-group
               [:div.controls
                [:textarea.span6 {:name "text" :id "text" :cols "80" :rows "10" :placeholder "Enter Comment"}]]]
              [:div.control-group
                [:div.controls
                   [:button.btn {:type "submit"} "Add Comment"]]]]))
        (build-comment-tree id comments)
      )))


(defn render-comment [com-map]
  (html5
    [:div.comment-block
     [:div.comment
       [:div.comment-head
          [:span
               [:a.arrow {:href (str "/comment/" (get com-map :_id) "/vote/up/")} "&#x25B2;"]
               [:span "&nbsp;"]
               [:a {:href (str "/user/" (get com-map :submitter) "/")} (get com-map :submitter)]
               [:span "&nbsp;"]
               [:span (string-date-formater (get com-map :created))]
               [:span "&nbsp;|&nbsp;"]
               [:a {:href (str "/comment/" (get com-map :_id) "/")} "Link/Reply"]]]
               
       [:div.comment-body
        [:span (get com-map :text)]]]]))


(defn comment-get [id]
  (let [com-map (comnt/get-comment-by-id id)]
    (layout
      (render-comment com-map)
      [:form {:method "POST" :action (str "/comment/" id "/")}
           [:input {:type "hidden" :name "parent-id" :value (get com-map :_id)}]
            [:input {:type "hidden" :name "post-id" :value (get com-map :post-id)}]
           [:div.control-group
             [:div.controls
              [:textarea.span6 {:name "text" :id "text" :cols "80" :rows "10" :placeholder "Enter Comment"}]]]
            [:div.control-group
              [:div.controls
                 [:button.btn {:type "submit"} "Add Comment"]]]])))

(defn comment-upvote [id]
  (if (users/logged-in?)
    (do
      (comnt/upvote id (users/get-username))
      (resp/redirect (str "/comment/" id "/")))
    (resp/redirect "/login/")))


(defn post-comment [parent-id text post-id]
  (if (users/logged-in?)
    (do
      (comnt/new-comment post-id (users/get-username) (escape-html text) parent-id)
      (resp/redirect (str "/post/" post-id "/")))
    (resp/redirect "/login/")))

(defn post-upvote [id]
  (if (users/logged-in?)
    (do 
      (posts/upvote id (users/get-username))
      (resp/redirect (str "/post/" id "/")))
    (resp/redirect "/login/")))
