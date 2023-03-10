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

(defn start [_req res]
  (.send res
         (-> template
             (.toString)
             (select-apply
               [".ui-section-header__layout>span" :setHTML
                [:a {:href "https://bootstrappingthis.com"} "bootstrappingthis.com"]]
               ["body" :appendChild
                (parse (render
                         [:<>
                          [:link {:rel "stylesheet" :href "/overlay.css"}]
                          [:div#ui-overlay]
                          [:style {:id "inline-styles"}]
                          [:script {:src "/js/main.js"
                                    :id "editor-code"}]]))]))))

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
