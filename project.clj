(defproject crypto-news "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [lib-noir "0.3.8"]
                 [com.novemberain/monger "1.4.2"]
                 [clj-time "0.4.4"]
                 [clojurewerkz/urly "1.0.0"]
                 [markdown-clj "0.9.19"]
                 [org.clojure/data.json "0.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler crypto-news.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
