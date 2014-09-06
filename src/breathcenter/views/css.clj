(ns breathcenter.views.css
  (:require
            [garden.core :as garden]
            [garden.stylesheet :as ss]
            [garden.color :as color]
            [garden.def :as def]))

(defn size
  ([height width]
     {:width width :height height})
  ([size]
   {:width size :height size}))

(defn flex-box
  ([align justify flow]
    {
     :display #{:flex :-webkit-flex}
     :align-items align
     :-webkit-align-items align
     :justify-content justify
     :-webkit-justify-content justify
     :flex-flow flow})
  ([align flow]
    {
     :display #{:flex :-webkit-flex}
     :align-items align
     :-webkit-align-items align
     :justify-content align
     :-webkit-justify-content align
     :flex-flow flow})
  ([align]
   {
    :display #{:flex :-webkit-flex}
    :align-items align
    :-webkit-align-items align
    :justify-content align
    :-webkit-justify-content align
    }
   ))

(def breathcenter (garden/css {:vendors ["webkit" "moz" "o" "ms"]}

                                        ;card flip stuff
                              [:.container ^:prefix {:width "260px"
                                                     :height "200px"
                                                     :position "relative"
                                                     :perspective "1000px"}]

                              [:#card ^:prefix {:position "absolute"
                                                :transform-style "preserve-3d"
                                                :transform-origin "right center"
                                                
                                                }]
                              [:.back ^:prefix {:transform "rotateY(180deg) translate3d(0, 0, .1px)"

                                                }]
                              [:.preserve-3d ^:prefix {:transform-style "preserve-3d"}]
                              [:.bfv {:backface-visibility "hidden"}]
                              [:.neutral ^:prefix {:transform "translate3d(0, 0, 0px)"}]
                              [:.chrome-visibility  ^:prefix {:transform "translate3d(0, 0, 1px)"}]

                              [:.figure ^:prefix {:display "block" :position "absolute" :backface-visibility "hidden"}]
                              [:.flipped ^:prefix {:transform "translateX( -100% ) rotateY( -180deg )"}]


                              [:html (merge {:font-family "arial"
                                             :margin 0} (size "100%"))]
                              [:.z-index {:z-index "99"}]
                              [:.selected {:color "white"}]
                              [:body {:margin 0}]

                              [:.trans {:transition "all .8s ease-in-out"}]
                              [:.flex ^:prefix (flex-box "center")]
                              [:.flex2 ^:prefix
                               (flex-box "")]
                              [:.column ^:prefix {:flex-flow "column"}]
                              [:.full (size "100%")]
                              [:#app ^:prefix {:perspective "1100px"
                                               :perspective-origin "50% 50%"
                                               :position "relative"
                                               }]
                              [:.button
                               {:background-color "white"
                                :width "10vw"
                                :height "6vh"
                                :margin-bottom "1vh"
                                :margin-left "1vh"
                                :border "0"
                                :font-size "2vw"
                                :outline "none"
                                :transition #{"all .15s ease-in"
                                              
                                              }
                                }
                               
                               [:&:hover ^:prefix
                                {:transform "scale(1.05)"
                                        ;:background-color "pink"
                                 }]
                               ]
                              [:.flex-start (flex-box "flex-start" "center" "")]
                              [:.flex-end (flex-box "flex-end" "center" "")]
                              [:.half (size "50%")]
                              
                              (ss/at-media {:min-width "320px"  :max-width "480px"}
                                           [:.vid-frame (size "50%" "100%")])

                              [:.aboutus-frame (size "100%" "50%")]
                              (ss/at-media {:min-width "320px"  :max-width "480px"}
                                           [:.aboutus-frame (size "50%" "50%")])

                              (ss/at-media {:min-width "320px"  :max-width "480px"}
                                           [:.un-mobile {:display "none"}])))
