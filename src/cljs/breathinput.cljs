(ns breathcenter.breathinput
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

(defn lift-and-send [owner ev]
  (let [chsk-send! (om/get-shared owner :chsk-send!)]
    
      (when (= (.-which ev) 13)
        (om/update-state! owner :trans not)
        (let [node (om/get-node owner "breath")
              val (.-value node)]
          (chsk-send! [:test/echo val])
          (set! (.-value node) ""))))) ;; this can be generalized to a
;; helper fn, johann

(defcomponent breath-input [data owner opts]
  
  (display-name [_] "breath-input")
  (render-state [_ state]
                (print (:bg state))
                (dom/div {:class "flex column"
                          :style {:width (str (:width opts) "%")
                                  :position "relative"
                                  :height (str (:height opts) "%")
                                  :border-radius "1%"
                                  :background (str "#" (:container-color opts))
                                  }}
                         
                         #_(dom/span {:class ""
                                      :style {:font-family "helvetica"
                                              :position (when (:trans state)  "absolute")
                                              :bottom (when (:trans state) 0)
                                              :zIndex 99
                                              :left (when (:trans state) "0")
                                              :background (if (:bg state) "#FF96A6" "#B6BFBF")
                                              :width "95%"
                                              :transition "all .5s cubic-bezier(.1,.79,.09,.87)"
                                        ;:border-radius "1%"
                                              :font-size "11vh"
                                              }}  "my breath feels like...")
                         (dom/input {
                                     :ref "breath"
                                     :type "text"
                                     :placeholder "my breath feels like..."
                                     :on-mouse-enter #(om/update-state! owner :bg not)
                                     :on-mouse-leave #(om/update-state! owner :bg not)
                                     :on-click #(om/set-state! owner :trans true)
                                     :on-key-down #(lift-and-send owner %)
                                     :style {

                                             :border (when (:bg state) "none")
                                             
                                             :outline 0
                                             :font-color (if (:trans state) "white" "")
                                             :background (if-not (:bg state)
                                                           (str "#" (:editable-color opts))
                                                           (str "#" "FFF" #_"FFA689"))
                                             :transition "all .8s cubic-bezier(.1,.79,.09,.87)"
                                             :border-radius "1%"
                                        ;:position (when (:trans state) "absolute")
                                        ;:bottom  (when (:trans state) "0")
                                             :font-size (if (:trans state) "4.95vw"  "4.85vw")
                                        ;:left (when (:trans state) "0")
                                             :width (if (:trans state) "76%"  "65%")
                                             :height (if (:trans state) "20%"  "15%")}})


                         )))



