(ns breathcenter.exp-components.funhouse
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.reader :as reader]
            [goog.events :as events]
            [goog.dom :as gdom]
            [breathcenter.doubleview :as dv]
            [breathcenter.user :as user]
            [breathcenter.breathinput :as bi]
            [breathcenter.exp-components.flip :as flip]
            [breathcenter.breathunit :as bu]
            [breathcenter.router :as router]
            [goog.debug :as debug]
            [breathcenter.panel :as panel]
            [cljs.core.async :as async :refer [chan put! pub sub pipe unique merge map< filter< alts! <!]]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [taoensso.sente :as sente :refer (cb-success?)]
            [om-tools.dom :as dom :include-macros true]
            [taoensso.encore :as encore :refer (logf)]
            [cognitect.transit :as t]
            
            ;[om.dom :as dom :include-macros true]
            ))
;; ex0 breaths are displayed in 1/3 width, breath input / sign-in
;; 2/3rds

;; breath

(def coord-process (fn [coord owner trans-out]
                    (go-loop [key (<! coord)]
                             
                             (case key
                               :toggle (om/set-state! owner :trans trans-out))
                             (recur (<! coord)))))

(defcomponent panel-toggler [data owner opts]
  (display-name [_] "panel-toggler")
  (did-mount [_]
             (om/set-state! owner :scale 1))
  
  (render-state [_ state]
                (let [nav (om/get-shared owner :nav-tokens)
                      toggler (fn [e]
                                
                                (put! (:coord opts) :toggle)
                                (om/set-state! owner :scale .5)
                                (js/setTimeout  #(put! nav (:next-route opts)) 300)
                                
                                )
                      ]
                  (dom/div {:class "flex z-index " 
                            :style {:position "absolute"
                                    :opacity 0.8
                                    :-webkit-transition (str "all " (:duration opts) "s " (:tween opts) )
                                    :transition (str "all " (:duration opts) "s " (:tween opts))
                                    :transform (str "scale(" (or (:scale state) 0.5) ")")
                                    :-webkit-transform (str "scale(" (or (:scale state) 0.5) ") "
                                                            "translate3d(0, 0, 1px)"
                                                            )
                                    :bottom (str (:bottom opts) "%")
                                    :right (str (:right opts) "%")
                                    :top (str (:top opts)  "%")
                                    :left (str (:left opts)  "%")
                                    :border-radius (str (:border-radius opts)  "%")
                                    :background (str "#" (:background opts))
                                    :width (str (:width opts) "vw")
                                    :height (str (:height opts) "vh")}
                            :on-click toggler
                                        
                            }
                           
                           (name (or (:route data) :life))))))


(def coord (chan))
(def coord-pub (pub coord identity))



(def toggler {:width 8
              :height 8
              :duration .05
              :tween "cubic-bezier(.1,.79,.09,.87)" 
              :background "3D4040"
              :next-route :out
              :border-radius 7.2
              :top 1.2
              :left 1.2})

(def breathunit {:sec-init-width 15
                 :sec-final-width 100
                 :tween "cubic-bezier(.1,.79,.09,.87)"
                 :duration .25
                 :cap-color "fff"
                 :sec-init-height 10
                 :sec-final-height 100
                 :content-bg-color "fff"})

(def breathdisplay {:width 30
                    :height 100
                    :coord (sub coord-pub :toggle (chan))
                    :trans-init {:y -100 :z -90}
                    :trans-in {:y 0 :z 0}
                    :trans-out {:y 110 :z -90}
                    :vert-stacked true
                    :duration .8
                    :coord-process coord-process
                    :z-index 1
                    :child {:component bu/breathpad
                            :cursor :breaths
                            :child-opts breathunit}
                    :color "790CE8"} )

  ;; input / signin
  
(def breath-input {:width 80
                   :height 80
                   
                   :editable-color "DAE5E5"
                   :container-color "FFF"})
(def breath-input-panel {
                         :coord (sub coord-pub :toggle (chan))
                         :duration .8
                         :color "B6BFBF"
                         :width 100
                         :height 100
                         :vert-stacked true
                         :trans-init {:y 110 :x 0 :z 0}
                         :trans-in {:y 0 :z 0 :x 0 :scale 1}
                         :trans-out {:y 110 :z -150 :scale 5}
                         :coord-process coord-process
                         :child {:component bi/breath-input
                                 :child-opts breath-input}
                         })
(def user-signin {:width 100
                  :height 100
                  :trans-init {:y -110 :x 0 :z 0}
                  :trans-in {:y 0 :z 0 :scale 1}
                  :trans-out {:y -110 :z -90 :scale 5}
                  :vert-stacked true
                  :coord (sub coord-pub :toggle (chan))
                  :coord-process coord-process
                  :duration .8             
                  :child {:component user/signin}
                  :color "790CE8"})

(def flipopts {:tween "cubic-bezier(.1,.79,.09,.87)"
               :vert-stacked true
               :width 70
               :height 100
               :duration .8
               :sub-fn #(sub coord-pub :toggle (chan))
               :flip-origin "right center"
               :back {:component panel/panel
                      :opts user-signin}
               
               :front {
                       :component panel/panel
                       :opts breath-input-panel}})

(def toggler {:width 8
              :height 8
              :duration .05
              :tween "cubic-bezier(.1,.79,.09,.87)" 
              :background "3D4040"
              :next-route :in
              :coord coord
              :border-radius 7.2
              :top 1.2
              :left 1.2})


(defcomponent exp0 [data owner opts]
  (render-state [_ state]
                (dom/div {:class "full flex preserve-3d"
                          :style {:overflow "hidden"}}
                         (dom/button {:class "chrome-visibility"
                                      :style
                                      {:position "absolute"
                                       :bottom 0
                                       :width "15vw"
                                       :height "10vh"
                                           
                                       :zIndex 99
                                       :left 0}
                                      :on-click (fn []
                                           (om/update-state! owner :toggle not))
                                      }  (str "toggle state: " (:toggle state)))
                         (om/build panel-toggler data {:opts toggler})
                         (om/build panel/panel data {:opts breathdisplay
                                                     :state (if (:toggle state)
                                                              {:width 80}
                                                              {:width nil}
                                                              )
                                                     }
                                   )
                        #_(om/build panel/panel data {:opts user-signin})
                         (om/build flip/flip data {:opts flipopts
                                                   
                                                   })
                         )))





;; exp 1 -> any view + drawer menu thing
