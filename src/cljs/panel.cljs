(ns breathcenter.panel
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.reader :as reader]
            [goog.events :as events]
            [goog.dom :as gdom]
            
            [breathcenter.animation :as ani]
            [breathcenter.bardo :as bardo]
            [goog.debug :as debug]
            [cljs.core.async :as async :refer [chan put! pipe unique merge map< filter< alts! <!]]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            ;[om.dom :as dom :include-macros true]
            ))







(defn transform-str [state {:keys [x y z scale] :or {x 0 y 0 z 0 scale 1}}]
  (str "scale(" (or (:scale state) scale) ")"  "translate3d(" (or (:x state)  x) "%," (or (:y state) y) "%," (or (:z state) z)  "px)" ))



(defn prefixer [prefixes thing]
  (conj (for [prefix prefixes]
      (keyword (str prefix "-" thing))
      ) thing))

(defn transformers [prefix val]
  (let [prefixed (map keyword (prefixer ["-moz" "-webkit"]  prefix))]
    (reduce (fn [style key]
              (assoc style key val))
                      {} prefixed)))
;; some opt input -> transstring


(defcomponent panel [data owner opts]
  (display-name [_] "panel")
  (init-state [_]
              (if-not (:coord opts)
                {:coord (chan)}
                {}))

  (did-mount [_]
             (when-not (:no-transition-in opts)   
               (js/setTimeout (fn [] (om/set-state! owner :trans (:trans-in opts)  
                                                   )) 16)))
  
  (will-mount [_]
              (when (:coord-process opts)
                (let [coord (:coord opts)
                      coord-process (:coord-process opts)]
                  (coord-process coord owner (:trans-out opts)))))
  
  (render-state [_ state]
                (let [transstr (transform-str (:trans state)
                                              (if (:no-transition-in opts)
                                                (:trans-in opts)
                                                (:trans-init opts)))]
                  
                    
                  (dom/section {:class "flex preserve-3d"
                                
                                :style {:background (str "#" (:color opts))
                                        :-webkit-transition (str "all " (:duration opts) "s " (:tween opts))
                                        :transition (str "all " (:duration opts) "s " (:tween opts))
                                        :position (when-not (:vert-stacked opts)   "absolute")
                                        :transform transstr
                                        
                                        
                                        :backface-visibility "hidden"
                                        :-webkit-transform transstr
                                        :zIndex (or (or (:z-index state)  (:z-index opts)) 0)
                                        :width (str (or (:width state)  (:width opts)) "%")
                                        :height (str (or (:height state)  (:height opts)) "%")}

                                }
                                 

                               (om/build (-> opts :child :component)
                                         ((or (:cursor (:child opts)) identity) data)
                                         {:opts (:child-opts (:child opts))})))))
