(ns breathcenter.user
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.reader :as reader]
            [goog.events :as events]
            [goog.dom :as gdom]
            
            
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


(defn clear! [input]
  (set! (.-value input) ""))

(defn clear-inputs [& inputs]
  (doseq [input inputs]
    (clear! input)))

(defn lift-and-send [owner msg-key]
  (let [un (om/get-node owner "un")
        pw (om/get-node owner "pw")
        chsk-send! (om/get-shared owner :chsk-send!)]
    (chsk-send! [msg-key {:username (.-value un)
                          :password (.-value pw)}])
    (clear-inputs un pw)
    ))

;; logic that transitions a signin / signup

(defn ornate [input & {:keys [left right]}]
  (dom/section {:style {:position "relative"
                        :margin-bottom "1vh"
                        :width "25vw"
                        :height "10vh"}
                
                
                }
               (dom/span {:style {:position "absolute"
                                  :top 0
                                  :left left
                                  ;:zIndex -1
                                  :right right
                                  ;:border-radius "50%"
                                  :width "2.5vw"
                                  :height "100%"
                                  :background "#fff"}})
               input))
(defn button-padder [button ]
  (dom/div {:class "flex"
            :style {:height "10vh"
                    :width "6.25vw"}}
           button))

(defn button [owner key label]
   
  (dom/button {:class "button"
               :on-click #(lift-and-send owner key)} label))


(defcomponent signin [data owner opts]
  (render-state [_ state]
                (let [nav-tokens (om/get-shared owner :nav-tokens)]
                  (dom/div {:class "flex column"
                            :style {    ;:background "grey"
                                    :width "36.5vw"
                                    
                                    :height "25vh"}
                            }
                           (dom/div {:class "flex-end"
                                     
                                     :style {;:margin-right "1.5vw"
                                             :width "40vw"}
                                     } 
                                    (dom/input {
                                                :ref "un"
                                                :type "text"
                                                :placeholder "username"
                                                :style {:width "25vw"
                                        ;:position "absolute"
                                                        :left ".6vw"
                                                        :top 0
                                                        ;:border "none"
                                                        :margin-bottom "1vh"
                                                        :outline 0
                                                        :height "10vh"
                                                        :font-size "4.95vw"}
                                                
                                                })
                                    (button owner :test/login "log-in")
                                    )
                           (dom/div {:class "flex-start"
                                     
                                     :style {;:margin-right "1.5vw"
                                             :width "40vw"}
                                     } 
                                    (dom/input {
                                                :ref "pw"
                                                :type "text"
                                                :placeholder "password"
                                                :style {:width "25vw"
                                        ;:position "absolute"
                                                        :top 0
                                        ;:left ".6vw"
                                                        ;:border "none"
                                                        :outline 0
                                                        :height "10vh"
                                                        :font-size "4.95vw"}})
                                    (button owner :test/user "sign-up")
                                    )
                           #_(dom/div {:class "column flex"}
                                     
                                    (button-padder  )
                                    (button-padder  ))
                           
                           ))

                ))
