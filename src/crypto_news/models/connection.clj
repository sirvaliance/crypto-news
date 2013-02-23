(ns crypto-news.models.connection
  (:require [monger.core :as mg]))


(defn db-connect []
  (do
    (mg/connect!)
    (mg/set-db! (mg/get-db "news"))))

