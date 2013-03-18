(ns crypto-news.views.utils
  (:require [clj-time.core :as cltime]
            [clj-time.coerce :as t-coerce]))
(defn time-diff [start-date]
  (long (cltime/in-minutes 
          (cltime/interval 
            start-date 
            (cltime/now)))))

(defn string-date-formater [start-date]
  (let [minutes (time-diff start-date)]
    (if (>= minutes 1440)
      (str (int (/ minutes 1440.0)) " Days Ago")
      (if (>= minutes 60) 
        (str (int (/ minutes 60.0)) " Hours Ago")
        (str minutes " Minutes Ago")))))



