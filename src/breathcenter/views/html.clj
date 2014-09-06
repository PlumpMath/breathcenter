(ns breathcenter.views.html
  (:require [hiccup.page :as html]
            [hiccup.element :as element]))


(defn cljs [space name]
  (if (= (System/getenv "MODE") "DEV")
    
    (conj
     '()
     (element/javascript-tag (str "goog.require('" space "." name "')"))
     (html/include-js  (str "/javascripts/" name "/"  "app.js"))
     (html/include-js (str "/javascripts/" name "/out/goog/base.js")))
    (html/include-js (str "/javascripts/app.js"))
    )
  )


(defn head-boiler [title css]
  [:head [:title title]
   [:meta {:name "viewport"
           :http-equiv "Content-type"
           :content "width=device-width, initial-scale=1.0"}]
   [:style css]
   ])

(defn breathcenter
  [css]
  (html/html5
   (head-boiler "breath center" css)
   #_(conj (head-boiler "breath center" css) ;; i commented this stuff
           ;; out because i did not have internet, future johann
         #_(html/include-js "//cdnjs.cloudflare.com/ajax/libs/fastclick/0.6.11/fastclick.min.js")
         #_(html/include-css "//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css")
         #_(element/javascript-tag "window.addEventListener('load', function() {
                                 FastClick.attach(document.body);
                                 }, false);"))
   [:body.full
    [:div#app.full]


    (when (= (System/getenv "MODE") "DEV")  (html/include-js "http://fb.me/react-0.11.0.js"))
    (cljs "breathcenter" "main")
    ]))
