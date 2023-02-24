(ns bootstrap.ui
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [applied-science.js-interop :as j]
    [garden.core :refer [css]]
    ["simply-beautiful" :as beautiful]
    ["react-color" :refer [BlockPicker]]
    [bootstrap.inline :refer [inline]]))

(defonce state (r/atom {:features [{} {}]}))

(def styles (inline "public/style.css"))

(def color-palette #js ["#EE4747" "#697689" "#2CBFD5" "#ff8a65" "#ba68c8" "#64b964" "#DDB235" "#E55388" "#578BC8" "#5E9B8B"])

(def fonts (js/JSON.parse (inline "public/fonts.json")))

(def icon {:check (inline "src/sprites/check.svg")
           :cross (inline "src/sprites/times.svg")
           :pencil (inline "src/sprites/pencil.svg")
           :download (inline "src/sprites/download.svg")
           :plus (inline "src/sprites/plus.svg")
           :up (inline "src/sprites/caret-up.svg")
           :down (inline "src/sprites/caret-down.svg")})

(defn swap-items
  [items i j]
  (assoc items i (items j) j (items i)))

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
        (swap! state assoc :hide-ui false)))
    100))

(defn component-icon [n]
  [:span.icon {:ref #(when % (j/assoc! % :innerHTML (n icon)))}])

(defn component-edit-text [state default-text coordinates]
  (let [value (or (get-in @state coordinates) default-text)]
  [:span value
   (when (not (:hide-ui @state))
     [:button.edit-button
      {:on-click (fn [ev]
                   (.preventDefault ev)
                   (.stopPropagation ev)
                   (let [new-value (js/prompt default-text value)]
                     (when new-value
                       (swap! state assoc-in coordinates new-value))))}
      [component-icon :pencil]])]))

(defn component-image-selection-modal [state]
  (let [img (r/atom nil)
        cancel-button [:button.neutral {:on-click (fn [_ev]
                                                    (reset! img nil)
                                                    (swap! state dissoc :image-select-modal))} "Cancel"]]
    (fn []
      (when (:image-select-modal @state)
        [:div.modal
         [:div.modal-inner
          [:label {:for "uploader"} "Choose an image."]
          [:input {:type "file"
                   :id "uploader"
                   :name "uploader"
                   :accept "image/png,image/jpeg,image/gif,image/*,.gif,.jpg,.jpeg,.png"
                   :on-change (fn [ev]
                                (let [;reader (js/FileReader.)
                                      file (-> ev .-target .-files first)]
                                  ;(j/assoc! reader :onload (fn [fev] (reset! img (j/get-in fev [:target :result]))))
                                  ;(.readAsDataURL reader file)
                                  (reset! img (.createObjectURL js/URL file))))}]
          [:div#preview (if @img
                          [:<> 
                           [:img {:src @img}]
                           [:span.button-row
                            cancel-button
                            [:button {:on-click (fn [_ev]
                                                  (swap! state
                                                         #(-> %
                                                              (assoc-in (:image-select-modal @state) @img)
                                                              (dissoc :image-select-modal)))
                                                  (reset! img nil))}
                             "Use this image"]]]
                          cancel-button)]]]))))

(defn component-style-editor [state]
  (when (not (:hide-ui @state))
    [:div
     ;[:h3 "Style editor"]
     [:fieldset
      [:> BlockPicker {:color (or (-> @state :style :brand-color) (first color-palette))
                       :triangle "hide"
                       :colors color-palette
                       :on-change-complete #(swap! state assoc-in [:style :brand-color] (j/get % :hex))}]
      [:div
       [:input {:id "dark-theme"
                :type :checkbox
                :checked (-> @state :style :dark-mode)
                :on-change #(swap! state update-in [:style :dark-mode] not)}]
       [:label {:for "dark-theme"} "Dark theme"]]]
     ;[:button {:on-click #(swap! state assoc :hide-ui true)} "Hide UI"]
     [:fieldset
      [:input {:list "font-list"
               :placeholder "Header font"
               :value (get-in @state [:style :brand-typeface-h])
               :on-change #(swap! state assoc-in [:style :brand-typeface-h] (-> % .-target .-value))}]]
     [:fieldset
      [:input {:list "font-list"
               :placeholder "Text font"
               :value (get-in @state [:style :brand-typeface])
               :on-change #(swap! state assoc-in [:style :brand-typeface] (-> % .-target .-value))}]]
     [:datalist {:id "font-list"}
      (doall
        (for [f fonts]
          [:option {:key (j/get f :family) :value (j/get f :family)}]))]
     [:fieldset
      [:button {:on-click #(download-page state)} [component-icon :download] "Download"]]
     [component-image-selection-modal state]]))

(defn component-logo-edit [_state]
  [:<>
   (when (not (:hide-ui @state))
     [:div.product-image-edit
      [:button.edit-button
       {:on-click (fn [ev]
                    (.preventDefault ev)
                    (swap! state assoc :image-select-modal [:page-icon]))}
       [component-icon :pencil]]])
   [:img {:src (or (:page-icon @state) "icon.svg")}]])

(defn component-email-box [state]
  [:<>
   [:form {:action "/sign-up" :class "ui-component-form ui-layout-grid ui-layout-column-2"}
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

(defn component-product-image [state classes coordinates]
  [:<>
   [:div.product-image {:class classes}
    [:div.product-image-edit
     (when (not (:hide-ui @state))
       [:button.edit-button
        {:on-click #(swap! state assoc :image-select-modal coordinates)}
        [component-icon :pencil]])]
    [:img {:src (or (get-in @state coordinates) "/img/placeholder.jpg")}]]])

(defn component-intro [state]
  [:div.ui-layout-container
   [:div.ui-layout-column-6.ui-layout-column-center
    [:h1 [component-edit-text state "Your app name" [:intro :title]]]
    [:p.ui-text-intro [component-edit-text state "Describe the value proposition of your app in a couple of sentences." [:intro :description]]]
    [:div.ui-component-cta.ui-layout-flex
     ; [:a {:href "/start" :class "ui-component-button ui-component-button-normal ui-component-button-primary"} "Your CTA"]
     [component-email-box state]]]
   [component-product-image state "ui-section-hero--image" [:intro :hero-image]]])

(defn component-feature-screenshot [i]
  [:div {:class (if (= (mod i 2) 0) "ui-image-half-left" "ui-image-right-half")}
   [component-product-image state nil [:features i :image]]])

(defn component-feature-description [state i]
  [:div
   [:h2 [component-edit-text state "Feature title" [:features i :title]]]
   [:p.ui-text-intro [component-edit-text state "Hello this is my intro text for this feature." [:features i :description]]]
   [:ul.ui-component-list.ui-component-list-feature.ui-layout-grid
    (for [t (range 4)]
      [:li.ui-component-list--item.ui-component-list--item-check
       {:key t}
       [component-edit-text state (str "This is item " t) [:features i :list t]]])]])
 
(defn component-feature-edit [i c feature]
  (when (not (:hide-ui @state))
    [:div.button-row
     (when (> i 0)
       [:button.edit-button
        {:on-click (fn [ev]
                     (.preventDefault ev)
                     (.stopPropagation ev)
                     (swap! state update-in [:features] swap-items i (dec i)))}
        [component-icon :up]])
     [:button.edit-button {:on-click (fn [_ev]
                                       (when (js/confirm "Are you sure you want to delete this feature?")
                                         (swap! state update-in [:features] (fn [features] (vec (remove #(= % feature) features))))))}
      [component-icon :cross]]
     (when (< i (dec c))
       [:button.edit-button
        {:on-click (fn [ev]
                     (.preventDefault ev)
                     (.stopPropagation ev)
                     (swap! state update-in [:features] swap-items i (inc i)))}
        [component-icon :down]])]))

(defn component-features [state]
  [:div.ui-layout-container
   (doall
     (let [c (count (:features @state))]
       (for [i (range c)]
         [:<>
          [:div.ui-section-feature__layout.ui-layout-grid.ui-layout-grid-2
           {:key i}
           (if (= (mod i 2) 0)
             [:<>
              [component-feature-screenshot i]
              [component-feature-description state i]]
             [:<>
              [component-feature-description state i]
              [component-feature-screenshot i]])
           [component-feature-edit i c (get-in @state [:features i])]]])))
   (when (not (:hide-ui @state))
     [:div.button-row
      [:button.edit-button
       {:on-click (fn [ev]
                    (.preventDefault ev)
                    (.stopPropagation ev)
                    (swap! state update-in [:features] conj {}))}
       [component-icon :plus]]])])

(defn component-outro [state]
  [:div.ui-layout-container
   [:div.ui-layout-column-center
    [:h2 [component-edit-text state "Closing message header" [:outro :title]]]
    [:p.ui-text-intro [component-edit-text state "Put your closing message here." [:outro :description]]]
    [:div.ui-component-cta.ui-layout-flex
     [component-email-box state]]]
   [component-product-image state "ui-section-hero--image" [:outro :hero-image]]])

(defn component-inline-styles [state]
  (str
    (when (:hide-ui @state)
      (str styles))
    (let [user-style (:style @state)]
      (str
        (str "@import url('https://fonts.googleapis.com/css2?family=" (:brand-typeface-h user-style) "&family=" (:brand-typeface user-style) "&display=swap');\n")
        (css [":root" (merge {:--ui-color-brand (or (:brand-color user-style) (first color-palette))
                              :--ui-typography-typeface-h (pr-str (or (:brand-typeface-h user-style) "Varela Round"))
                              :--ui-typography-typeface (pr-str (or (:brand-typeface user-style) "Arial"))}
                             (if (:dark-mode user-style)
                               {:--ui-color-n-000 "#1a1a1a"
                                :--ui-color-n-025 "#050505"
                                :--ui-color-n-050 "#0a0a0a"
                                :--ui-color-n-100 "#141414"
                                :--ui-color-n-300 "#aeaeae"
                                :--ui-color-n-500 "#cacaca"
                                :--ui-color-n-900 "#e5e5e5"}
                               {:--ui-color-n-000 "#fff"
                                :--ui-color-n-025 "#fafafa"
                                :--ui-color-n-050 "#f5f5f5"
                                :--ui-color-n-100 "#ebebeb"
                                :--ui-color-n-300 "#aeaeae"
                                :--ui-color-n-500 "#353535"
                                :--ui-color-n-900 "#1a1a1a"}))])))))

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
