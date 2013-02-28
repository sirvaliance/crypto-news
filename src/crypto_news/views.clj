(ns crypto-news.views
  (:use hiccup.core 
        hiccup.page
        hiccup.util
        compojure.response
        crypto-news.settings)
  (:require [noir.response :as resp]
            [noir.session :as session]
            [noir.cookies :as cook]
            [noir.util.crypt :as crypt]
            [clj-time.core :as cltime]
            [crypto-news.models.users :as users]
            [crypto-news.models.posts :as posts]
            [crypto-news.models.comments :as comnt]))

(defn layout [& content]
  (html5
    [:head
     [:title "Crypto News"]
      (include-css "/css/bootstrap.min.css")
      (include-css "/css/bootstrap-responsive.min.css")
      (include-css "/css/style.css")]
    [:body
     [:div#menu.navbar.navbar-fixed-top
      [:div.navbar-inner
       [:div.container
        [:div
         [:ul.nav
          [:li 
           [:a {:href "/"} "Crypto News"]]
          [:li
           [:a {:href "/new/"} "New"]]
          [:li
           [:a {:href "/comments/"} "Comments"]]
          [:li
           [:a {:href "/post/new/"} "Submit"]]]
         [:ul.nav.pull-right
            (if (users/logged-in?)
              (html5 [:li
                      [:a {:href (str "/user/" (users/logged-in?))} (str "(421) " (users/logged-in?))]]
                     [:li
                      [:a {:href "/logout/"} "Logout"]])
              (html5 [:li
                       [:a {:href "/login/"} "Login"]]
                     [:li
                       [:a {:href "/signup/"} "Signup"]]))]
          ]]]]

     [:div#main-wrapper.container.container-fluid
      [:div#content
       content]]]))

(defn string-date-formater [start-date]
  (let [minutes (long (cltime/in-minutes (cltime/interval start-date (cltime/now))))]
    (if (>= minutes 1440)
      (str (int (/ minutes 1440.0)) " Days Ago")
      (if (>= minutes 60) 
        (str (int (/ minutes 60.0)) " Hours Ago")
        (str minutes " Minutes Ago")))))
      

(defn render-index-post [post-map & post-num]
  (html5
    [:div.news-item.row-fluid
      [:div.span12
        [:div.news-item-vote
          [:span.news-num (if (seq post-num) (str (first post-num)  "."))]
          [:a.arrow {:href (str "/post/" (get post-map :_id) "/vote/up/")} "&#x25B2;"]]
        [:div.news-item-link
          [:a {:href (get post-map :url)} (get post-map :title)]
          [:span "&nbsp;"]
          [:span (str "(" (get post-map :url-host) ")")]]
        [:div.news-item-info
          [:span (str (get post-map :karma) " Points")]
          [:span "by"]
          [:span
           [:a {:href (str "/user/" (get post-map :submitter))} (get post-map :submitter)]]
          [:span (string-date-formater (get post-map :created))]
          [:span
           [:a {:href (str "/post/" (get post-map :_id) "/")} (str (get post-map :comments) " Comments")]]]]
      ]))

(defn index []
  (layout
    (let [posts-list (posts/get-post-all)]
     ; Should contain obvious ranking algorithm
     ; http://amix.dk/blog/post/19574
      (for [i (range (count posts-list))]
        (render-index-post (nth posts-list i) (+ i 1))))))

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
           (cook/put-signed! cookie-key :user (get user :username))
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

(defn user-profile-edit
  [user]
  (if (.equals "" (users/logged-in?))
    (resp/redirect "/login/")
    (layout
      [:form.form-horizontal
       [:fieldset
        [:legend (str "Your Profile")]
        [:div.control-group
         [:label.control-label {:for "username"}  "Username:"]
         [:div.controls
          [:span#username.uneditable-input (get user :username)]]]
        [:div.control-group
         [:label.control-label {:for "created"} "Created:"]
         [:div.controls
          [:label#created.label-pad-top (get user :created)]]]
        [:div.control-group
         [:label.control-label {:for "karma-comment"} "Comment Karma"]
         [:div.controls
          [:label#karma-comment.label-pad-top (str (get user :karma-comment))]]]
       [:div.control-group
        [:div.controls
          [:a {:href (str "/user/comments/" (get user :username))} (str "Comments by " (get user :username))]]]
       [:div.control-group
         [:label.control-label {:for "karma-submissions"} "Submission Karma"]
         [:div.controls
          [:label#karma-submissions.label-pad-top (str (get user :karma-submission))]]]
       [:div.control-group
        [:div.controls
          [:a {:href (str "/user/submissions/" (get user :username))} (str "Submissions by " (get user :username))]]]
       [:div.control-group
         [:label.control-label {:for "email"} "Email:"]
         [:div.controls
          [:input.span6 {:name "email" :type "text" :id "email" :placeholder "Email"  :value (get user :email)}]]]
       [:div.control-group
         [:label.control-label {:for "profile"} "Profile:"]
         [:div.controls
          [:textarea.span6 {:name "profile" :id "profile" :cols "80" :rows "10" :placeholder "Profile Info (Links, Twitter, Etc)" :value (get user :profile)}]]]
        [:div.control-group
         [:label.control-label {:for "gpg-pubkey"} "GPG Public Key"]
         [:div.controls
          [:textarea.span6 {:name "gpg-pubkey" :id "gpg-pubkey" :cols "80" :rows "10" :placeholder "Your GPG Public Key" :value (get user :gpg-pubkey)}]]]
         [:div.control-group
            [:div.controls
               [:button.btn {:type "submit"} "Update"]]]
       ]])))

(defn user-profile-view
  [user]
  (layout 
    [:h1 (str "Profile for " (get user :username))]))

(defn user-get [username]
  (let [user (users/get-user (str username))]
    (if (and (.equals username (get user :username)) (.equals username (users/logged-in?)))
      (user-profile-edit user)
      (user-profile-view user))))

(defn new-post-get[]
  (if (.equals "" (users/logged-in?))
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
  (if (posts/find-post-by-url url)
    ; Throw error saying it has been used
    (resp/redirect "/post/new/")
    (do 
      ;Make sure the user is logged in
      (let [id (posts/new-post (escape-html title) (escape-html url) (escape-html text) (users/logged-in?))]
        (resp/redirect (str "/post/" id "/"))))))
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
                   [:a.comment-arrow {:href (str "/comment/" (get comment-head :_id) "/vote/up/")} "&#x25B2;"]
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
      (render-index-post post-map)
      [:form {:method "POST" :action (str "/comment/" id "/")}
           [:input {:type "hidden" :name "parent-id" :value id}]
           [:input {:type "hidden" :name "post-id" :value id}]
           [:div.control-group
             [:div.controls
              [:textarea.span6 {:name "text" :id "text" :cols "80" :rows "10" :placeholder "Enter Comment"}]]]
            [:div.control-group
              [:div.controls
                 [:button.btn {:type "submit"} "Add Comment"]]]]
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
  (comnt/upvote id (users/logged-in?))
  (resp/redirect (str "/comment/" id "/")))

(defn post-comment [parent-id text post-id]
  (do
    (comnt/new-comment post-id (users/logged-in?) (escape-html text) parent-id)
    (resp/redirect (str "/post/" post-id "/"))))

(defn post-upvote [id]
  (posts/upvote id (users/logged-in?))
  (resp/redirect (str "/post/" id "/")))
