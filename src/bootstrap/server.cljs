(ns bootstrap.server 
  (:require
    ["fs" :as fs]
    [applied-science.js-interop :as j]
    [promesa.core :as p]
    [sitefox.html :refer [render parse select-apply]]
    [sitefox.web :as web]
    [sitefox.logging :refer [bind-console-to-file]]))

(bind-console-to-file)

(defonce server (atom nil))

(def template (fs/readFileSync "public/index.html"))

(defn component-homepage []
  [:<>
   [:section.ui-section-hero
    [:div.ui-layout-container
     [:div.ui-layout-column-6.ui-layout-column-center
      [:h1 "App loading."]]]]
   [:script {:src "/js/main.js"}]])

(defn component-email-box []
  [:<>
   [:form {:action "/sign-up" :class "ui-component-form ui-layout-grid ui-layout-column-4"}
    [:input {:type "email" :placeholder "Email" :class "ui-component-input ui-component-input-medium" :required true}]
    [:button {:type "submit" :class "ui-component-button ui-component-button-medium ui-component-button-primary"} "Join waitlist"]]
   [:p {:class "ui-text-note"} [:small "Some encouraging words."]]])

(defn start [_req res]
  (.send res
         (-> template
             (.toString)
             (select-apply
               [".ui-section-header__layout>span" :setHTML
                [:a {:href "https://bootstrappingthis.com"} "bootstrappingthis.com"]]
               [".ui-component-cta.ui-layout-flex" :setHTML [component-email-box]]
               ["body" :appendChild
                (parse (render
                         [:<>
                          [:link {:rel "stylesheet" :href "/overlay.css"}]
                          [:div#ui-overlay]
                          [:script {:src "/js/main.js"}]]))]))))

(defn setup-routes [app]
  (web/reset-routes app)
  (j/call app :get "/start" start)
  (web/static-folder app "/" "public"))

(defn main! []
  (p/let [[app host port] (web/start)]
    (reset! server app)
    (setup-routes app)
    (println "Serving on" (str "http://" host ":" port))))

(defn ^:dev/after-load reload []
  (js/console.log "Reloading.")
  (setup-routes @server))
