(ns breathcenter.breathunit
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require 
            [goog.events :as events]
            [goog.dom :as gdom]
            [cognitect.transit :as t]
            [breathcenter.animation :as ani]
            [goog.debug :as debug]
            [cljs.core.async :as async :refer [chan put! pipe unique merge map< filter< alts! <!]]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            ;[om.dom :as dom :include-macros true]
            ))


;; fade in effect


(defn rand-color []
  (let [randy #(rand-int 255)]
      (str "rgb(" (randy)  "," (randy)   ","  (randy) ")")))

(defcomponent breathunit [data owner opts]
  (display-name [_] "panel")
  (did-mount [_]
             (js/setTimeout (fn []
                              (om/set-state! owner :opacity 1))  32))
  (render-state [_ state]
                #_(dom/span {:class "flex"
                             :style {:background (rand-color)
                                     :display "inline-flex"
                                   
                                     :width (str  (:sec-init-width opts) "vw")
                                     :height (str (:sec-final-height opts) "vh")}
                             }  (:breath data))
                (dom/section {
                              :style {:position (if (:toggled state)
                                                  "absolute"
                                                  "relative")
                                      :opacity (or (:opacity state) 0)
                                      :top 0
                                      :left 0
                                      :zIndex (when (:toggled state) 1)
                                      :display "inline-flex"b
                                      :transition  (str "all " (:duration opts) "s " (:tween opts))
                                      :width  (if (:toggled state)
                                                (str (:sec-final-width opts) "%")
                                                (str (:sec-init-width opts)  "vw"))
                                      :height  (if (:toggled state)
                                                 (str (:sec-final-height opts) "%")
                                                 (str (:sec-init-height opts) "vh"))
                                      
                                      
                                      }}

                             #_(dom/span {:on-touch-end #(om/update-state! owner :toggled not)
                                          :style {:position "absolute"
                                                  :top 0
                                                  :width "1.2vw"
                                                  :height "100%"
                                                  :border ".25px solid #000"
                                                
                                                  :background (str "#" (:cap-color opts))
                                                  :left 0
                                        
                                                  :border-radius "50%"
                                                  }})
                             #_(dom/span {
                                          :style { :position "absolute"
                                                  :top 0
                                                  :width "1.2vw"
                                                  :height "100%"
                                                  :border ".25px solid #000"
                                                  :background (str "#" (:cap-color opts))
                                                  :right "-1.2vw"
                                        
                                                  :border-radius "50%"
                                                  }})

                             (dom/article {:class "flex"
                                        ;:on-mouse-enter #(om/set-state! owner :toggled true)
                                        ;:on-mouse-leave #(om/set-state! owner :toggled false)
                                           :on-click #(om/update-state! owner :toggled not)
                                           :style {:position "absolute"
                                                   :background (rand-color) #_(str "#" (:content-bg-color opts))
                                                   :left ".6vw"
                                                   :border ".25px solid #000"
                                                   :width "100%"
                                                   :height "100%"}}
                              
                                          
                                          (:breath data)
                                          ))))

(defcomponent breathpad [data owner opts]
  (render-state [_ state]
                (dom/section {:class "full"
                              :style {:overflow "scroll"}
                              }
                             (om/build-all breathunit data {:opts opts})
                             )))
