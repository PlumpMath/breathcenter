(ns breathcenter.router
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [>! <! put! chan sliding-buffer pub sub]]
            [om.core :as om :include-macros true]
            [goog.events :as events]
            [goog.debug :as debug]
            
            
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true])
  (:import goog.history.EventType
           goog.history.Html5History))

(enable-console-print!)


(defn update-title! [{:keys [new-state]}]
  (let [{:keys [route]} new-state]
    (set! js/document.title (name route))))

(defn update-history! [owner {:keys [new-state old-state]}]
  (let [history (om/get-state owner :history)
        {:keys [route]} new-state]
    (if-not (= old-state new-state)
      
      (if route
        (.setToken history (name route))
        (.setToken history "/"))
      (.replaceToken history (name route)))))

(defn- stop-router!
  "Takes an Om component with a history object
  Disables the history object."
  [owner]
  (let [history (om/get-state owner :history)]
    ; Remove goog.events listeners from history object.
    (events/removeAll history)
    ; Disable history object.
    (.setEnabled history false)))

(defn start-history! [data owner]
  (let [history (doto (Html5History.)
                  (.setUseFragment false)
                  (.setPathPrefix "")
                  (.setEnabled true))
        nav-tokens-chan (om/get-shared owner :nav-tokens)]
    (events/listen history EventType.NAVIGATE
                        (fn [event]
                          (when (.-isNavigation event)
                            (put! nav-tokens-chan (.-token event)))))
    (om/set-state! owner :history history)))



(defn start-tx-loop!
  "listens for nav related transactions"
  [owner]
  (let [transactions-pub-chan (om/get-shared owner :transactions-pub)
        txs (sub transactions-pub-chan :nav (chan))]
    (go-loop [tx (<! txs)]
             (update-title! tx)
             (update-history! owner tx)
             (recur (<! txs)))))



(defn build-page [data {views :page-views} owner]
  (let [view   (:route data)
        view (case view
               "/index.html" nil
               "/" nil
               view)
        
        ]
    
    (om/build ((or view :begin) views) data)))



(defn start-nav-loop! [{:keys [route] :as data} owner]
  (let [nav-tokens-chan (om/get-shared owner :nav-tokens)]
    (go-loop [token (<! nav-tokens-chan)]
             
          (om/update! data :route token :nav)
             (recur (<! nav-tokens-chan))
             )))











(defn start-router! [data owner]
  (start-history! data owner)
  (start-nav-loop! data owner))

(defcomponent router [data owner opts]
  (display-name [_] "router")
  (will-mount [_]
               (start-tx-loop! owner))
  (render [_] (build-page data opts owner))
  (did-mount [_] (start-router! data owner))
  (will-unmount [_] (stop-router! owner)))
