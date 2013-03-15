(ns crypto-news.views.utils
  (:require [clj-time.core :as cltime]
            [clj-time.coerce :as t-coerce]))


(defn string-date-formater [start-date]
  (let [minutes (long (cltime/in-minutes (cltime/interval start-date (cltime/now))))]
    (if (>= minutes 1440)
      (str (int (/ minutes 1440.0)) " Days Ago")
      (if (>= minutes 60) 
        (str (int (/ minutes 60.0)) " Hours Ago")
        (str minutes " Minutes Ago")))))



