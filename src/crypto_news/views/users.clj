(ns crypto-news.views.users
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


(defn user-profile-edit
  [user]
    (layout
      [:form.form-horizontal {:action "/user/update/" :method "POST"}
       [:fieldset
        [:legend (str "Your Profile")]
        [:div.control-group
         [:label.control-label {:for "username"}  "Username:"]
         [:div.controls
          [:label#username.label-pad-top (get user :username)]]]
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
       ]]))

(defn user-update
  [email profile gpg-pubkey]
  (println email))

(defn user-profile-view
  [user]
  (layout
    [:form.form-horizontal
     [:fieldset
      [:legend (str "Profile for " (get user :username))]
      [:div.control-group
       [:label.control-label {:for "username"}  "Username:"]
       [:div.controls
        [:label#username.label-pad-top (get user :username)]]]
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
        [:label {:name "email" :type "text" :id "email"} (get user :email)]]]
     [:div.control-group
       [:label.control-label {:for "profile"} "Profile:"]
       [:div.controls
        [:pre.span6 (get user :profile)]]]
      [:div.control-group
       [:label.control-label {:for "gpg-pubkey"} "GPG Public Key"]
       [:div.controls
        [:pre.span6 (get user :gpg-pubkey)]]]
     ]]))

(defn user-get [username]
  (let [user (users/get-user (str username))]
    (if (and (users/logged-in?) (.equals username (get user :username)) (.equals username (users/get-username)))
      (user-profile-edit user)
      (user-profile-view user))))


