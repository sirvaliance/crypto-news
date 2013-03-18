(ns crypto-news.views.common
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
            [clj-time.coerce :as t-coerce]
            [crypto-news.models.users :as users]
            [crypto-news.models.comments :as comnt]))


(defn layout [& content]
  (html5
    [:head
     [:title "Cypherpunk News"]
     (include-css "/css/bootstrap.min.css")
     (include-css "/css/bootstrap-responsive.min.css")
     (include-css "/css/style.css")
     (include-js "/js/jquery-1.9.1.min.js")]
    [:body
     [:div#menu
      [:div
       [:div.container
        [:ul.nav.pull-left.nav-pills
         [:li 
          [:a {:href "/"} "&oplus; Cypherpunk News"]]
         [:li
          [:a {:href "/new/"} "New Posts"]]
         (if (users/logged-in?) [:li [:a {:href "/post/new/"} "Submit"]])
         ]
        [:ul.nav.pull-right.nav-pills
         (if (users/logged-in?)
           (html5 [:li
                   [:a {:href (str "/user/" (users/get-username) "/")} (str (users/get-username))]]
                  [:li
                   [:a {:href "/logout/"} "Logout"]])
           (html5 [:li
                   [:a {:href "/login/"} "Login"]]
                  [:li
                   [:a {:href "/signup/"} "Signup"]]))]
        ]]]

     [:div#main-wrapper.container.container-fluid
      [:div#content
       content]]
     [:div#main-footer.container.container-fludi
      "Footer Stuff"]]))


