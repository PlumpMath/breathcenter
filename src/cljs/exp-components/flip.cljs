(ns breathcenter.exp-components.flip
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.reader :as reader]
            [goog.events :as events]
            [goog.dom :as gdom]
            [breathcenter.doubleview :as dv]
            [breathcenter.user :as user]
            [breathcenter.breathinput :as bi]
            
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
            
            
            ))



(defn builder [id-key data opts pred]
  (let [passed-opts (-> opts id-key :opts)
        sub (:sub-fn opts)
        ]
    (om/build (-> opts id-key :component) data  {:opts
                                                 (if pred
                                                   passed-opts
                                                   (assoc passed-opts
                                                     :no-transition-in true
                                                     :trans-in {:x 0 :y 0 :z 1}
                                                     
                                                     :coord (when sub  (sub))
                                                     ))

                                                 })))

;; this is wasteful because both views do not need to be bound to the
;; dom

;; ok i fixed it now i have to think about a new scenario i can't word
;; right now

;;have a transition solution right now but it looks too campy perhaps

;; i need to give the component predicate knowledge of when its
;; first mounted and when its child components are being toggled

(defcomponent flip [data owner opts]
  (init-state [_]
              {:first-mounted true}) ;;name better
  (render-state [_ state]
                
                (dom/section {:class "preserve-3d"
                              :style {
                                      :position (when-not (:vert-stacked opts) "absolute")
                                      :width (str (or (:width state)  (:width opts)) "%")
                                      :height (str (or (:height state)   (:height opts)) "%")
                                      }}
                             (dom/button {:class "chrome-visibility"
                                          :style
                                          {:position "absolute"
                                           :bottom 0
                                           :width "15vw"
                                           :height "10vh"
                                           
                                           :zIndex 99
                                           :left 0}
                                          :on-click (fn []
                                                      
                                                      (om/set-state! owner :first-mounted false)
                                                      (om/update-state! owner :flip not))
                                          }  (str "flip state: " (:flip state)))
                             (dom/div {:id "card"
                                       :class (if (:flip state)
                                                "full flipped"
                                                "full ")
                                       :style {
                                               :transform-origin (:flip-origin opts)
                                               :transition (str "all " (:duration opts) "s "  (:tween opts))
                                               }}
                                      
                                      (dom/div {:class "figure full front"}
                                               (when-not (:flip state) 
                                                 (builder :front data opts (:first-mounted state))))
                                      (dom/div {:class "figure full back"}
                                               (when (:flip state)
                                                 (builder :back data opts (:first-mounted state))))))))
