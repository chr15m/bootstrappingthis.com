(ns bootstrap.ui
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [applied-science.js-interop :as j]
    [bootstrap.inline :refer [inline]]
    ["simply-beautiful" :as beautiful]))

(defonce state (r/atom {}))

(def styles (inline "public/style.css"))

(defn download-page [state]
  (swap! state assoc :hide-ui true)
  ; allow the UI time to update
  (js/setTimeout
    (fn []
      (let [html (-> (js/XMLSerializer.) (.serializeToString js/document))
            html (.html beautiful html)
            a (.createElement js/document "a")
            body (.-body js/document)]
        (.setAttribute a "download" "index.html")
        (j/assoc! a :href (str "data:text/html;charset=UTF-8," (js/encodeURIComponent html)))
        (.appendChild body a)
        (.click a)
        (.removeChild body a)
        (swap! state assoc :hide-ui false)))))

(defn component-edit-text [state default-text _coordinates]
  [:span default-text
   (when (not (:hide-ui @state))
     [:span.edit-button
      {:on-click (fn [ev]
                   (.preventDefault ev)
                   (.stopPropagation ev)
                   (js/alert "edit mode"))}
      "🖉"])])

(defn component-style-editor [state]
  (when (not (:hide-ui @state))
    [:div
     [:div "Style editor"]
     [:button {:on-click #(swap! state assoc :hide-ui true)} "Hide UI"]
     [:button {:on-click #(download-page state)} "Download"]]))

(defn component-logo-edit [_state]
  [:<>
   [:img {:src "icon.svg"}]
   "[EDIT]"])

(defn component-email-box [state]
  [:<>
   [:form {:action "/sign-up" :class "ui-component-form ui-layout-grid ui-layout-column-4"}
    [:input {:type "email" :placeholder "Email" :class "ui-component-input ui-component-input-medium" :required true}]
    [:button {:type "submit"
              :class "ui-component-button ui-component-button-medium ui-component-button-primary"
              :on-click (fn [ev]
                          (.preventDefault ev)
                          (js/alert "The user's email will be stored when they click this button.")
                          false)}
     [component-edit-text state "Join waitlist" [:email-box :button-text]]]]
   [:p {:class "ui-text-note"}
    [:small [component-edit-text state "Encouraging words here." [:email-box :encouraging-words]]]]])

(defn component-product-image [classes]
  [:<>
   [:img.product-image {:src "/img/screenshot-1.png" :class classes}]
   "[EDIT]"])

(defn component-intro [state]
  [:div.ui-layout-container
   [:div.ui-layout-column-6.ui-layout-column-center
    [:h1 [component-edit-text state "Your app name" [:intro :title]]]
    [:p.ui-text-intro [component-edit-text state "Describe the value proposition of your app in a couple of sentences." [:intro :description]]]
    [:div.ui-component-cta.ui-layout-flex
     ; [:a {:href "/start" :class "ui-component-button ui-component-button-normal ui-component-button-primary"} "Your CTA"]
     [component-email-box state]]]
   [component-product-image "ui-section-hero--image"]])

(defn component-feature-screenshot [i]
  [:div {:class (if (= (mod i 2) 0) "ui-image-half-left" "ui-image-right-half")}
   [component-product-image]])

(defn component-feature-description [state i]
  [:div
   [:h2 [component-edit-text state "Feature title" [:features i :title]]]
   [:p.ui-text-intro [component-edit-text state "Hello this is my intro text for this feature." [:features i :description]]]
   [:ul.ui-component-list.ui-component-list-feature.ui-layout-grid
    (for [t (range 4)]
      [:li.ui-component-list--item.ui-component-list--item-check
       {:key t}
       [component-edit-text state (str "This is item " t) [:features i :list t]]])]])

(defn component-features [state]
  [:div.ui-layout-container
   (doall
     (for [i (range 3)]
       [:div.ui-section-feature__layout.ui-layout-grid.ui-layout-grid-2
        {:key i}
        (if (= (mod i 2) 0)
          [:<>
           [component-feature-screenshot i]
           [component-feature-description state i]]
          [:<>
           [component-feature-description state i]
           [component-feature-screenshot i]])]))])

(defn component-outro [state]
  [:div.ui-layout-container
   [:div.ui-layout-column-6.ui-layout-column-center
    [:h2 [component-edit-text state "Closing message header" [:outro :title]]]
    [:p.ui-text-intro [component-edit-text state "Put your closing message here." [:outro :description]]]
    [:div.ui-component-cta.ui-layout-flex
     [component-email-box state]]]
   [component-product-image "ui-section-hero--image"]])

(defn component-inline-styles [state]
  (when (:hide-ui @state) (str styles)))

(defn start {:dev/after-load true} []
  (rdom/render [component-style-editor state] (js/document.querySelector "#ui-overlay"))
  (rdom/render [component-inline-styles state] (js/document.querySelector "#inline-styles"))
  (rdom/render [component-logo-edit state] (js/document.querySelector "#logo"))
  (doseq [[q component] [["#intro" component-intro]
                         ["#features" component-features]
                         ["#outro" component-outro]]]
    (rdom/render [component state] (js/document.querySelector q))))

(defn main! []
  (start))
