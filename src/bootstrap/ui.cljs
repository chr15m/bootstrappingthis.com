(ns bootstrap.ui
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

(defonce state (r/atom {}))

(defn component-main []
  [:<>
   [:div "Page editor"]
   [:div "Hide this"]])

(defn start {:dev/after-load true} []
  (rdom/render [component-main state] (js/document.querySelector "#ui-overlay")))

(defn main! []
  (start))
