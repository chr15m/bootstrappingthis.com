(ns bootstrap.ui
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(defonce state (r/atom {}))

(defn component-editor []
  [:<>
   [:div "Page editor"]
   [:div "Hide this"]])

(defn component-email-box [button-text subtext]
  [:<>
   [:form {:action "/sign-up" :class "ui-component-form ui-layout-grid ui-layout-column-4"}
    [:input {:type "email" :placeholder "Email" :class "ui-component-input ui-component-input-medium" :required true}]
    [:button {:type "submit" :class "ui-component-button ui-component-button-medium ui-component-button-primary"}
     (or button-text "Join waitlist")]]
   [:p {:class "ui-text-note"}
    [:small (or subtext "Some encouraging words.")]]])

(defn component-product-image [classes]
  [:<>
   [:img.product-image {:src "/img/screenshot-1.png" :class classes}]
   "[EDIT]"])

(defn component-cta-1 []
  [:div.ui-layout-container
   [:div.ui-layout-column-6.ui-layout-column-center
    [:h1 "Your app name [EDIT]"]
    [:p.ui-text-intro "Describe the value proposition of your app in a couple of sentences. [EDIT]"]
    [:div.ui-component-cta.ui-layout-flex
     ; [:a {:href "/start" :class "ui-component-button ui-component-button-normal ui-component-button-primary"} "Your CTA"]
     [component-email-box "CTA button text [EDIT]" "Encouraging words [EDIT]"]]]
   [component-product-image "ui-section-hero--image"]])

(defn component-feature-screenshot [i]
  [:div {:class (if (= (mod i 2) 0) "ui-image-half-left" "ui-image-right-half")}
   [component-product-image]])

(defn component-feature-description []
  [:div
   [:h2 "Feature title [EDIT]"]
   [:p.ui-text-intro "Hello this is my intro text for this feature. [EDIT]"]
   [:ul.ui-component-list.ui-component-list-feature.ui-layout-grid
    (for [t (range 4)]
      [:li.ui-component-list--item.ui-component-list--item-check
       {:key t}
       (str "This is item " t " [EDIT]")])]])

(defn component-features []
  [:div.ui-layout-container
   (doall
     (for [i (range 3)]
       [:div.ui-section-feature__layout.ui-layout-grid.ui-layout-grid-2
        {:key i}
        (if (= (mod i 2) 0)
          [:<>
           [component-feature-screenshot i]
           [component-feature-description]]
          [:<>
           [component-feature-description]
           [component-feature-screenshot i]])]))])

(defn component-cta-2 []
  [:div.ui-layout-container
   [:div.ui-layout-column-6.ui-layout-column-center
    [:h2 "Closing message header [EDIT]"]
    [:p.ui-text-intro "Put your closing message here. [EDIT]"]
    [:div.ui-component-cta.ui-layout-flex
     [component-email-box "CTA button text [EDIT]" "Encouraging words [EDIT]"]]]
   [component-product-image "ui-section-hero--image"]])

(defn component-logo-edit [_state]
  [:<>
   [:img {:src "icon.svg"}]
   "[EDIT]"])

(defn start {:dev/after-load true} []
  (rdom/render [component-editor state] (js/document.querySelector "#ui-overlay"))
  (rdom/render [component-logo-edit state] (js/document.querySelector "#logo"))
  (doseq [[q component] [["#cta-1" component-cta-1]
                         ["#features" component-features]
                         ["#cta-2" component-cta-2]]]
    (rdom/render [component state] (js/document.querySelector q))))

(defn main! []
  (start))
