
(defproject breathcenter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [figwheel "0.1.3-SNAPSHOT"]
                 [ring "1.1.8"]
                 [clj-time "0.6.0"]
                 [org.clojure/core.memoize "0.5.6"]
                 [java-jdbc/dsl "0.1.0"]
                 [org.postgresql/postgresql "9.2-1003-jdbc4"]
                 [org.clojure/java.jdbc "0.3.2"]
                 [com.impossibl.pgjdbc-ng/pgjdbc-ng "0.3" :classifier "complete"]
                 [clj-dbcp "0.8.1"]
                 [com.cognitect/transit-clj "0.8.247"]
                 [com.cognitect/transit-cljs "0.8.168"]

                 [org.clojure/core.async "0.1.319.0-6b1aca-alpha"]
                 [prismatic/om-tools "0.3.0"]
                 [http-kit "2.1.13"]
                 [com.taoensso/sente "0.15.1"]
                 [ring/ring-anti-forgery         "1.0.0"]
                 [com.taoensso/encore       "1.7.0"]
                 [com.stuartsierra/component "0.2.1"]
                 [org.clojure/clojurescript "0.0-2280"]
                 [garden "1.1.5"]
                 [om "0.7.0"]
                 [hiccup "1.0.5"]
                 [servant "0.1.3"]
                 [fogus/ring-edn "0.2.0"]
                 [hiccup-bridge "1.0.0-SNAPSHOT"]
                 [com.facebook/react "0.11.1"]
                 [com.andrewmcveigh/cljs-time "0.1.6"]
                 [hiccups "0.3.0"]
                 ]
  :plugins [
            [lein-cljsbuild "1.0.3"]
            [lein-figwheel "0.1.3-SNAPSHOT"]
            
            ]
  :main breathcenter.server

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :compiler {
                                   :output-to "resources/public/javascripts/main/app.js"
                                   :output-dir "resources/public/javascripts/main/out"
                                   :source-map "resources/public/javascripts/main/app.js.map"
                                   :optimizations :none
                                   }} 

                       {:id "release"
                        :source-paths ["src/cljs"]
                        :compiler {
                                   :output-to "resources/public/javascripts/app.js"
                                   :output-dir "resources/public/javascripts/"
                                   :source-map "resources/public/javascripts/app.js.map"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :preamble ["react/react.min.js"]
                                   :externs ["react/externs/react.js"]}}
                       ]})
