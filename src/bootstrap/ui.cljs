(ns bootstrap.ui
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

(defonce state (r/atom {}))

(defn component-main []
  [:<>
   [:section.ui-section-hero
    [:div.ui-layout-container
     [:div.ui-layout-column-6.ui-layout-column-center
      [:h1 "Your landing page"]
      [:p [:input {:placeholder "Your startup name"}]]
      [:p [:input {:placeholder "Your unique landing page URL"}]]
      [:button "Create your page"]]]]])

(defn start {:dev/after-load true} []
  (rdom/render [component-main state]
               (js/document.querySelector "main")))

(defn main! []
  (start))
