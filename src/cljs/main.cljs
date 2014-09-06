(ns breathcenter.main
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.reader :as reader]
            [goog.events :as events]
            [goog.dom :as gdom]
            [breathcenter.doubleview :as dv]
            [breathcenter.user :as user]
            [breathcenter.breathinput :as bi]
            [breathcenter.exp-components.flip :as flip]
            [breathcenter.breathunit :as bu]
            [breathcenter.exp-components.funhouse :as fun]
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
(enable-console-print!)
(js/React.initializeTouchEvents true)

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" ; Note the same path as before
       {:type :auto ; e/o #{:auto :ajax :ws}
       })]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

(defn transity [[id sig] data]
  (om/transact! data :breaths #(into [] (conj % ((t/read (t/reader :json) sig) "data")))))







(defonce app-state (atom {:breaths #_[] (into [] (map (fn [b] {:breath b})  (take 60 (repeatedly #(rand-nth ["i am the kosmos" "dove kosmos?" "everything is illuminated"])))  )) }))

(defcomponent breath-presentation [data owner]
  (render-state [_ _]
     (dom/h3 "inhale human")           
     ))


(def coord-process (fn [coord owner trans-out]
                    (go-loop [key (<! coord)]
                             
                             (case key
                               :toggle (om/set-state! owner :trans trans-out))
                             (recur (<! coord)))))




(defn coord-opts [coord opts]
  (assoc opts :coord coord))









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
                           
                           (name (or (:route data) :life))))

                ))

(defcomponent breath-in [data owner opts]
  (render-state [_ _]
                (let [coord-breath-input (chan)
                      breath-input {:width 80
                                    :height 80
                                    
                                    :editable-color "DAE5E5"
                                    :container-color "FFF"}

                      breath-input-panel {:tween "cubic-bezier(.1,.79,.09,.87)"
                                          :duration .8
                                          :color "B6BFBF"
                                          :width 100
                                          :height 100
                                          :trans-init {:y -100 :z -150}
                                          :trans-in {:y 0 :z 0}
                                          :trans-out {:y 110 :z -150}
                                          
                                          :child {:component bi/breath-input
                                                  :child-opts breath-input}
                                          
                                          :coord-process coord-process
                                          }
                      other-toggler {:width 8
                                     :height 8
                                     :background "797F7F"
                                     :duration .025
                                     :tween "cubic-bezier(.1,.79,.09,.87)" 
                                     :next-route :signin
                                     :border-radius 7.2
                                     :bottom 1.2
                                     :left 1.2}
                      breath-panel-toggler {:width 8
                                            :height 8
                                            :background "797F7F"
                                            :duration .025
                                            :tween "cubic-bezier(.1,.79,.09,.87)" 
                                            :next-route :out
                                            :border-radius 7.2
                                            :top 1.2
                                            :left 1.2}
                      ]
                  (dom/div {:class "full"}
                           (om/build panel-toggler data {:opts (coord-opts coord-breath-input other-toggler)})
                           (om/build panel-toggler data {:opts (coord-opts coord-breath-input breath-panel-toggler) })
                           (om/build panel/panel data {:opts (coord-opts coord-breath-input breath-input-panel)
                                                       :init-state {:color "#E2C0E8"
                                                                    :width 100
                                                                    :height 100}})))))



(defcomponent breath-out [data owner opts]
  (render-state [_ _]
                (let [coord-breath-output (chan)
                      breath-output-panel {:tween "cubic-bezier(.1,.79,.09,.87)" 
                                           :duration .8
                                           :color "B6BFBF"
                                           :trans-init {:x -100 :z -90}
                                           :trans-in {:x 0 :z 0}
                                           :trans-out {:x 110 :z -90}
                                           :width 100
                                           :z-index 1
                                           :height 100
                                           
                                           :child {:component breath-presentation}
                                                        
                                           :coord-process coord-process}
                      breath-outpanel-toggler {:width 8
                                               :height 8
                                               :duration .05
                                               :tween "cubic-bezier(.1,.79,.09,.87)" 
                                               :background "3D4040"
                                               :next-route :begin
                                               :border-radius 7.2
                                               :top 1.2
                                               :left 1.2}
                      ]
                  (dom/div {:class "full"}
                           (om/build panel-toggler data {:opts (coord-opts coord-breath-output breath-outpanel-toggler) })
                           (om/build panel/panel data {:opts (coord-opts coord-breath-output breath-output-panel) })))   
                ))

(defcomponent begin [data owner opts]
  (render-state [_ state]
                (let [coord (chan)
                      breathunit {:sec-init-width 10
                                  :sec-final-width 100
                                  :tween "cubic-bezier(.1,.79,.09,.87)"
                                  :duration .1
                                  :cap-color "fff"
                                  :sec-init-height 10
                                  :sec-final-height 100
                                  :content-bg-color "fff"}
                      begintoggler {:width 8
                                    :height 8
                                    :duration .05
                                    :tween "cubic-bezier(.1,.79,.09,.87)" 
                                    :background "3D4040"
                                    :next-route :in
                                    :border-radius 7.2
                                    :top 1.2
                                    :left 1.2}
                      beginpanel {:width 100
                                  :height 100
                                  :trans-init {:y -100 :z -90}
                                  :trans-in {:y 0 :z 0}
                                  :trans-out {:y 110 :z -90}
                                  :coord-process coord-process
                                  :duration .8             
                                  :z-index 1
                                  :child {:component bu/breathpad
                                          :cursor :breaths
                                          :child-opts breathunit}
                                  :color "790CE8"} ]
                  (dom/div {:class "full"}
                           (om/build panel-toggler data {:opts (coord-opts coord begintoggler)} )
                           #_(om/build bu/breathpad data {:opts breathunit})  
                           (om/build panel/panel data {:opts (coord-opts coord beginpanel)})))))

(defcomponent signin [data owner opts]
  (render-state [_ state]
                (let [coord (chan)
                      
                      begintoggler {:width 8
                                    :height 8
                                    :duration .05
                                    :tween "cubic-bezier(.1,.79,.09,.87)" 
                                    :background "3D4040"
                                    :next-route :out
                                    :border-radius 7.2
                                    :top 1.2
                                    :left 1.2}
                      beginpanel {:width 100
                                  :height 100
                                  :trans-init {:y -100 :z -90}
                                  :trans-in {:y 0 :z 0}
                                  :trans-out {:y 110 :z -90}
                                  :coord-process coord-process
                                  :duration .8             
                                  :z-index 1
                                  :child {:component user/signin
                                          
                                          ;:child-opts breathunit
                                          }
                                  :color "790CE8"} ]
                  (dom/div {:class "full"}
                           (om/build panel-toggler data {:opts (coord-opts coord begintoggler)}) 
                           
                           (om/build panel/panel data {:opts (coord-opts coord beginpanel)})))))
;;is it a stupid move to have all my opts stuff inside a renderstate?

(defcomponent test [data owner opts]
  
  
  (render-state [_ state] 
                (let [coord (chan)
                      coord-pub (pub coord identity)
                     
                      
                      begintoggler {:width 8
                                    :height 8
                                    :duration .05
                                    :tween "cubic-bezier(.1,.79,.09,.87)" 
                                    :background "3D4040"
                                    :next-route :out
                                    :coord coord
                                    :border-radius 7.2
                                    :top 1.2
                                    :left 1.2}
                      breath-input {:width 80
                                    :height 80
                                    
                                    :editable-color "DAE5E5"
                                    :container-color "FFF"}
                      breath-input-panel {
                                          :coord (sub coord-pub :toggle (chan))
                                          :duration .8
                                          :color "B6BFBF"
                                          :width 100
                                          :height 100
                                          :trans-init {:y 110 :x 0 :z 0}
                                          :trans-in {:y 0 :z 0 :x 0 :scale 1}
                                          :trans-out {:y 110 :z -150 :scale 5}
                                          
                                          :child {:component bi/breath-input
                                                  :child-opts breath-input}
                                          
                                          :coord-process coord-process
                                          }

                      userpanel {:width 100
                                  :height 100
                                  :trans-init {:y -110 :x 0 :z 0}
                                  :trans-in {:y 0 :z 0 :scale 1}
                                  :trans-out {:y -110 :z -90 :scale 5}
                                  :coord-process coord-process
                                  
                                  :duration .8             
                                  :coord (sub coord-pub :toggle (chan))
                                  :child {:component user/signin
                                          
                                        ;:child-opts breathunit
                                          }
                                  :color "790CE8"}
                      opts {:tween "cubic-bezier(.1,.79,.09,.87)"
                            :duration .8
                            :flip-origin "right center"
                            :back {:component panel/panel
                                    :opts userpanel}
                            :sub-fn #(sub coord-pub :toggle (chan))
                            :front {
                                   :component panel/panel
                                   :opts breath-input-panel}}

                      ]
                  (dom/div {:class "full"}
                           (om/build panel-toggler data {:opts begintoggler}) 
                           (om/build flip/flip data {:opts opts}))
                  


                  )))







(def main-components {;:signin signin
                      
                      :begin fun/exp0  #_begin 
                      :in begin #_signin  #_breath-in 
                      :out breath-out})


 



(defcomponent app-view [data owner]
  (init-state [_] {:coord (chan)})
  (will-mount [_]
              (let [ event-handler (fn [[id sig :as ev] _]
                                             
                                             (case id
                                        ;:chsk/state (chsk-send! [:test/echo])
                                               :chsk/recv (transity sig data)
                                               
                                               (logf "Event: %s" ev)))

                    ]

                (sente/start-chsk-router-loop! event-handler ch-chsk)
                ))
  
  (render-state [_ state]
                
                (om/build router/router data {:opts {:page-views main-components}}) 
                ))





#_(when (= (.-hostname js/location) "localhost")
    (fw/watch-and-reload
     ;; :websocket-url "ws://localhost:3449/figwheel-ws" default
     :jsload-callback (fn [] (print "america"))))

(let [transactions (chan)
      transactions-pub (pub transactions :tag)]
  (om/root
   app-view
   app-state
   {:target (.querySelector js/document "#app")
    :tx-listen #(do
                  (print %)
                  (put! transactions %))
    :shared {:nav-tokens (chan)
             :transactions transactions
             :chsk-send! chsk-send!  
             :transroute (chan)
             :transactions-pub transactions-pub}}))




