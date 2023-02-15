(ns bootstrap.server 
  (:require
    ["fs" :as fs]
    [applied-science.js-interop :as j]
    [promesa.core :as p]
    [sitefox.html :refer [render-into]]
    [sitefox.web :as web]
    [sitefox.logging :refer [bind-console-to-file]]))

(bind-console-to-file)

(defonce server (atom nil))

(def template (fs/readFileSync "public/index.html"))
(def default-template (fs/readFileSync "public/template/index.html"))

(defn component-homepage []
  [:<>
   [:section.ui-section-hero
    [:div.ui-layout-container
     [:div.ui-layout-column-6.ui-layout-column-center
      [:h1 "Coming soon. Maybe."]]]]
   #_ [:script {:src "/js/main.js"}]])

(defn start [_req res]
  (.send res (render-into template "main" [component-homepage])))

(defn setup-routes [app]
  (web/reset-routes app)
  ;(j/call app :get "/$" #(.send %2 (render-into template "body" [component-homepage])))
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
