(ns breathcenter.ws
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [breathcenter.views.complector :as views]
            [breathcenter.db.db :as db]
           
            [ring.util.response :refer [file-response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [taoensso.sente :as sente]
            [ring.middleware.anti-forgery :as ring-anti-forgery]
            [clojure.core.async :refer [chan <! timeout  >! put! close! go-loop go]]
            [org.httpkit.server :as http-kit]
            [ring.middleware.reload :as reload]
            [ring.util.response :as resp]))

(defn- logf [fmt & xs] (println (apply format fmt xs)))
(defonce dbmap  (db/notification-listen)) 


(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
                connected-uids]}
        (sente/make-channel-socket! {})]
    (def ring-ajax-post                ajax-post-fn)
    (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
    (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
    (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
    (def connected-uids                connected-uids) ; Watchable, read-only atom
    )

(defn broadcast-process [listch]
  (go-loop [payload (<! listch)]
           (println "HELLO FROM CHANNEL")
           (doseq [uid (:any @connected-uids)]
             (chsk-send! uid [:some/thing payload]))

           (recur (<! listch))))


(defn event-msg-handler
  [{:as ev-msg :keys [ring-req event ?reply-fn]} _]
  (let [session (:session ring-req)
        uid     (:uid session)
        [id data :as ev] event]
        (println "uid " uid)
        (println  ev)
        
        ;; compare passwords -> put the latest session id in usermap
        
        (case id
          :test/echo (db/create-breath data uid  dbmap)
          :test/user (db/create-user (assoc data :sessid uid) dbmap)
          :test/login (let [{:keys [username]} data]
                        ;; logic that checks for accurate passwords
                        (db/update-breather-sessid uid username dbmap)
                        )
          (logf "Event: %s" ev)
          )))
