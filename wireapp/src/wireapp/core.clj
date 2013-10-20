(ns wireapp.core
  (:require [wire.routing :as r]
            [wire.middleware :as m]
            [wire.helpers :as h :refer [path-for]]
            [hiccup.core :as html]
            [hiccup.page :as page]
            [clojure.tools.logging :as log]
            [ring.util.response :refer (file-response resource-response status)]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.head :refer [wrap-head]]))

(defn resources [path]
  {:path "*"
   :get #(resource-response (str path "/" (get-in % [:params :*])))})

(defn files [path]
  {:path "*"
   :get #(file-response (str path "/" (get-in % [:params :*])))})

(defn render [body & {:keys [status headers]}]
  {:body body :status (or status 200) :headers (or headers {})})

(defn render-html [body & {:keys [status headers]}]
  (render body :status status :headers (merge headers {"Content-Type" "text/html"})))

(defn navigation [rq]
  (html/html [:ul
              [:li [:a {:href (path-for rq [])} "home"]]
              [:li [:a {:href (path-for rq [:about])} "about"]]
              [:li [:a {:href (path-for rq [:categories])} "categories"]]
              [:li [:a {:href (path-for rq [:categories :one] {:format "csv"})} "category I"]]
              [:li [:a {:href (path-for rq [:categories :two] {:format "pdf"})} "category II"]]
              [:li [:a {:href (path-for rq [:resources] {:* ["app.css"]})} "app.css"]]]))

(defn page [rq title content]
  (render-html
   (page/html5
    (page/include-css (str (path-for rq []) "app.css"))
    [:head [:title title]]
    [:body [:div {:id "container"}
            [:div {:id "left"} (navigation rq)]
            [:div {:id "right"}
             [:p [:h3 title]
              content]]]])))

(defn child-links [rq]
  (html/html
   [:ul
    (for [child (r/children (h/current rq))]
      [:li [:a {:href (path-for rq (conj (h/ids rq) (r/id child))
                                {:format "xml"})}
            (r/id child)]])]))

(def app-routes
  (r/root
   ;; Root handlers...
   {:get (fn [rq] (page rq "Wire Example" (path-for rq [] {})))}
   ;; Child routes...
   [:about {:get (fn [rq] (page rq "About" "Hi there."))}]
   [:categories {:get (fn [rq]
                        (page rq "Categories" (child-links rq)))}
    [:one {:path "I.:format"
           :get (fn [rq] (page rq "one" "I"))}]
    [:two {:path "II.:format"
           :get (fn [rq] (page rq "two" "II"))}]]
   ;; static asset handlers...
   [:resources (resources "public")]
   [:files (files "resources/public")]))

(defn wrap-route-logger [h]
  (fn [r]
    (log/infof "Matched route: %s" (:route-context r))
    (h r)))

(def handler
  (-> m/wrap-exec-route
      (wrap-route-logger)
      (m/wrap-identify-route app-routes)
      (wrap-file-info)
      (wrap-content-type)
      (wrap-head)))
