

(ns breathcenter.db.db
  (:import com.impossibl.postgres.api.jdbc.PGNotificationListener
           
           org.postgresql.util.PGobject
           [java.io ByteArrayInputStream ByteArrayOutputStream])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core.async :as async :refer [chan put! go >!]]
            [clj-dbcp.core :as dbcp]
            [cognitect.transit :as transit]
            [java-jdbc.ddl :as ddl]
            [java-jdbc.sql :as sql]))



(defn value->transit [value]
  (let [ out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer value)
    (.toString out)))

(defn transit->value [transit]
  (let [ in (ByteArrayInputStream. 4096)
        reader (transit/reader in  :json)]
    (transit/reader reader  (:data transit))
    (.toString in)))

(extend-protocol jdbc/ISQLValue
  clojure.lang.IPersistentVector
  (sql-value [value]
    (doto (PGobject.)
      (.setType "json")
      (.setValue (value->transit value))))
  clojure.lang.IPersistentMap
  (sql-value [value]
    (doto (PGobject.)
      (.setType "json")
      (.setValue (value->transit value)))))



(def db-credentials (-> "resources/dbconfig.edn"
                        slurp
                        clojure.core/read-string))



(def db-spec {:subprotocol "pgsql" 
              :classname "com.impossibl.postgres.jdbc.PGDriver"
              :subname (or (System/getenv "SUBNAME")  (:subname db-credentials)) 
              :user  (or (System/getenv "USER") (:user db-credentials)) 
              :password (or (System/getenv "PW") (:password db-credentials))})



(defn uuid [] (str (java.util.UUID/randomUUID)))






(defn load-schema [dbmap]
  (let [sql (slurp "resources/testschema.sql")]
    (jdbc/db-do-commands {:connection (:dbconn dbmap)} sql)))





(defn get-all-examples [connection]
    (into []
          (jdbc/query {:connection (:dbconn connection)} ["SELECT * FROM example"])))


(defn query-sessid [sessid connection] 
  (first (jdbc/query {:connection (:dbconn connection)}
                     ["SELECT username FROM breather WHERE sessid = ?" sessid])))



(defn create-breath [breath sessid  connection]
  (println "BREATH CREATED")
  (jdbc/db-do-prepared {:connection (:dbconn connection)} "INSERT INTO example (data) VALUES (?);" [(merge
                                                                                                     (query-sessid sessid connection)                                                                                                   {:breath breath
                                                                                                                                                                                                                                         :date (java.util.Date.)})]))

(defn query-user [username connection] 
  (first (jdbc/query {:connection (:dbconn connection)}
                     ["SELECT * FROM breather WHERE username = ?" username]
                )))




(defn create-user [{:keys [username password sessid]} connection]
  (try 
    (jdbc/insert! {:connection (:dbconn connection)} :breather
                  [:username :password :sessid]
                  [username password sessid])
    (catch Exception _))
  (println "query user: "  (query-user username connection))
  (println "query sessid: "  (query-sessid sessid connection))

  )





(defn update-breather-sessid [sessid username connection ]
  (try
    (jdbc/update! {:connection (:dbconn connection)}
                  :breather
                  {:sessid sessid}
                  ["username=?" username])
    (catch Exception _)))

(defn notification-listen []
  (println "connecting")
  (println "creating listener")
  (let [pgconn (jdbc/get-connection db-spec)
        listch (chan)
        listener (proxy [PGNotificationListener] []
                   (notification [pid channel payload]
                     (println (format "pid: '%s'  channel: '%s'" pid channel))
                     (put! listch payload)))]

    (println "Registering listener")
    (.addNotificationListener pgconn listener)

    (println "Enabling notify")
    (jdbc/execute! {:connection pgconn} ["LISTEN exampleupdate"])
    
    {:dbconn pgconn :listener listener :listch listch}))




(comment
  (jdbc/db-do-commands db-spec "DROP TABLE example;")
  (jdbc/db-do-commands db-spec "CREATE TABLE example (data json);")

  (jdbc/db-do-prepared db-spec "INSERT INTO example (data) VALUES (?);" [{:foo "bar"}])


  (println  (jdbc/query db-spec [ "SELECT * FROM example;"])))












