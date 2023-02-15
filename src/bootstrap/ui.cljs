(ns bootstrap.ui
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

(defonce state (r/atom {}))

(defn button-clicked [_ev]
  (swap! state update-in [:number] inc))

(defn component-main [state]
  [:div
   [:h1 "Validate your startup idea"]
   [:p "Create a simple landing page to validate your startup idea."]
   [:a {:href "/start"} "Create your page"]
   [:button {:on-click button-clicked} "click me"]])

(defn start {:dev/after-load true} []
  (rdom/render [component-main state]
               (js/document.getElementById "app")))

(defn main! []
  (start))
