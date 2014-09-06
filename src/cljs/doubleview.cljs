(ns breathcenter.doubleview
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.reader :as reader]
            [goog.events :as events]
            [goog.dom :as gdom]
            
            
            [breathcenter.breathinput :as bi]
            [breathcenter.user :as user]
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






;; want to generalize this component for any two components
;; pass in the opts states






(defcomponent doubleview [data owner opts]
  (init-state [_] {:switch true})
  (render-state [_ state]
                (let [components (:components opts)
                      main (components 0)
                      slide-in (components 1)
                      main-trans (:main-trans opts)
                      other-trans (:other-trans opts)
                      thing (assoc slide-in :trans-in {:x 50 :y 0 :z 0 :scale 0.505}  #_(:toggled other-trans))
                      thing2 (assoc main :trans-in {:x 50 :y 0 :z 0 :scale 0.505}) ;;forgive my shitty naming, future self

                      ] 
                  (dom/div {:class "full"
                                        ;:on-click #(toggler owner %)
                            }
                             
                           (dom/button {:style {:position "absolute"
                                                :zIndex 99
                                                :width "45vw"
                                                :bottom 0
                                                :left 0}
                                        :on-click #(do
                                                     (om/update-state! owner :toggle not)
                                                     (if (:on state) 
                                                       (js/setTimeout (fn [] (om/update-state! owner :on not)) 300)
                                                       (om/update-state! owner :on not)
                                                       ))

                                        } (str
                                        "on: " (:on state)
                                        "toggle: " (:toggle state)))
                           (dom/button {:style {:position "absolute"
                                                :zIndex 99
                                                :bottom 0
                                                :right 0}
                                        :on-click #(do
                                                     (om/update-state! owner :switch not)
                                                     )
                                        } (str "switch " (:switch state)))
                           (dom/div {:class ""
                                        ;:style {:height "100%"}
                                     :on-click (fn [ev]
                                                 (when (:toggle state)
                                                   (om/set-state! owner :switch true)
                                                   (om/update-state! owner :toggle not)
                                                   (if (:on state) 
                                                     (js/setTimeout (fn [] (om/update-state! owner :on not)) 300)
                                                     (om/update-state! owner :on not)
                                                     ))

                                                 )
                                     }
                            
                                    (if (:switch state)
                                      (om/build panel/panel data {:opts main
                                                                  :state (if (:toggle state)
                                                                           {:trans (:toggled main-trans)
                                                                    
                                                                    
                                                                            }
                                                                           {:trans (:default main-trans)
                                                                            :z-index 1
                                                                    
                                                                            }
                                                                           )
                                                          
                                                          
                                                                  }

                                                )
                                      (when (:on state) 
                                        (om/build panel/panel data {:opts thing2
                                                                    :state (if-not (:toggle state)
                                                                             {:trans (:default other-trans)
                                                                              :z-index 1}
                                                                             {:trans (:toggled other-trans)
                                                                              }
                                                                             )
                                                            
                                                                    }

                                                  ))))
                             
                           (dom/div {:class ""
                                        ;:style {:height "100%"}
                                     :on-click (fn [ev]
                                                 (when (:toggle state)
                                                  
                                                   (om/set-state! owner :switch false)
                                                   (js/setTimeout #(om/update-state! owner :toggle not) 50)
                                                   (if (:on state) 
                                                     (js/setTimeout (fn [] (om/update-state! owner :on not)) 300)
                                                     (om/update-state! owner :on not)
                                                     )))
                                     

                                     } 
                                    (if-not (:switch state)
                                      (om/build panel/panel data {:opts thing
                                                                  :state (if (:toggle state)
                                                                           {:trans (:toggled main-trans)}
                                                                           {:trans (:default main-trans)
                                                                            :z-index 1}
                                                                           )
                                                                  
                                                                  })
                                      (when (:on state)
                                        (om/build panel/panel data {:opts thing
                                                                    :state (if-not (:toggle state)
                                                                             {:trans (:default other-trans)
                                                                              :z-index 1}
                                                                             {:trans (:toggled other-trans)}
                                                                             )
                                                                    


                                                                    })
                                        ))))))



  )
