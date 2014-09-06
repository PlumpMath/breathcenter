(ns breathcenter.views.complector
  (:require
            [breathcenter.views.css :as css]
            [breathcenter.views.html :as page]))

(defn breathcenter
  []
  (page/breathcenter css/breathcenter))
