(ns breathcenter.server
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [breathcenter.views.complector :as views]
            [breathcenter.db.db :as db]
            [breathcenter.ws :as ws]
            [ring.util.response :refer [file-response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [taoensso.sente :as sente]
            [ring.middleware.anti-forgery :as ring-anti-forgery]
            [clojure.core.async :refer [chan <! timeout  >! put! close! go-loop go]]
            [org.httpkit.server :as http-kit]
            [ring.middleware.reload :as reload]
            [ring.util.response :as resp]) )


(defonce breathcenter (atom {:server nil 
                         :db nil}))



;;helper examples from https://github.com/seancorfield/om-sente/blob/master/src/clj/om_sente/server.clj


(defn unique-id ;;temporarily using rand-int
  "Return a really unique ID (for an unsecured session ID).
  No, a random number is not unique enough. Use a UUID for real!"
  []
  (rand-int 10000))

(defn session-uid
  "Convenient to extract the UID that Sente needs from the request."
  [req]
  (get-in req [:session :uid]))

  

(defn index
  "Handle index page request. Injects session uid if needed."
  [req]
  {:status 200
   :session (if-let [uid  (session-uid req)]
              
              (:session req)
              (assoc (:session req) :uid (unique-id)))
   :body (views/breathcenter)})



(defroutes breathcenterroutes
  (GET "/" req (#'index req))
  (GET  "/chsk" req (ws/ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ws/ring-ajax-post                req))
  (route/resources "/" {:root "public"}))


(def app (-> breathcenterroutes
             (ring-anti-forgery/wrap-anti-forgery
              {:read-token (fn [req] (-> req :params :csrf-token))})
             handler/site
             reload/wrap-reload))




(defonce chsk-router
  (sente/start-chsk-router-loop! ws/event-msg-handler ws/ch-chsk))




(defn -main
  ([port]
     (let [Port (Integer/parseInt port)
           server (http-kit/run-server app {:port Port})
           ]
       
       (db/load-schema ws/dbmap)
       (ws/broadcast-process (:listch ws/dbmap))
       (println "server running w/ port" port)
       (swap! breathcenter assoc :server server)))
  ([]
     (let [strPort (System/getenv "PORT")
           Port (Integer/parseInt strPort)]
       (println "we're running on:" strPort)
       (http-kit/run-server app {:port Port}))))

(defn server [cmd & {:keys [port]}]
  (let [silencio #((:server @breathcenter))]
    (case cmd
      :kill (silencio)
      :pheonix (do
                 (silencio)
                 (-main (str port)))
      )))


