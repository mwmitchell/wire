(ns wireapp.core
  (:require [wire.routing :as r]
            [wire.middleware :as m]
            [wire.helpers :as h :refer [path-for]]
            [hiccup.core :as html]
            [hiccup.page :as page]
            [clojure.tools.logging :as log]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.head :refer [wrap-head]]))

(defn resources [path]
  {:path "*"
   :get #(resource-response (str path "/" (get-in % [:params :*])))})

(defn render [body & {:keys [status headers]}]
  {:body body :status (or status 200) :headers (or headers {})})

(defn render-html [body & {:keys [status headers]}]
  (render body :status status :headers (merge headers {"Content-Type" "text/html"})))

(defn nav-item [label ids & [params]]
  [:li [:a {:href (path-for ids params)} label]])

(defn navigation []
  (html/html
   [:ul
    (nav-item "home" [])
    (nav-item "about" [:about])
    (nav-item "categories" [:categories])
    (nav-item "category I" [:categories :one] {:format "csv"})
    (nav-item "category II" [:categories :two] {:format "pdf"})
    (nav-item "app.css" [:resources] {:* ["app.css"]})]))

(defn page [title content]
  (render-html
   (page/html5
    (page/include-css (str (path-for [:resources] {:* ["app.css"]})))
    [:head [:title title]]
    [:body [:div {:id "container"}
            [:div {:id "left"} (navigation)]
            [:div {:id "right"}
             [:p [:h3 title]
              content]]]])))

(defn child-links []
  (html/html
   [:ul
    (for [child (r/children (h/current))]
      [:li [:a {:href (path-for (conj (h/ids) (r/id child))
                                {:format "xml"})}
            (r/id child)]])]))

(def app-routes
  (r/root
   ;; Root handlers...
   {:get (fn [_] (page "Wire Example" (path-for [] {})))}
   ;; Child routes...
   [:about {:get (fn [_] (page "About" "Hi there."))}]
   [:categories {:get (fn [_]
                        (page "Categories" (child-links)))}
    [:one {:path "I.:format"
           :get (fn [_] (page "one" "I"))}]
    [:two {:path "II.:format"
           :get (fn [_] (page "two" "II"))}]]
   ;; static asset handling
   [:resources (resources "public")]))

(defn wrap-route-logger [h]
  (fn [r]
    (log/infof "Wire route context: %s" (dissoc m/*context* :routes))
    (h r)))

(def handler
  (-> m/wrap-exec-route
      (wrap-route-logger)
      (m/wrap-bind-identified-route app-routes)
      (wrap-file-info)
      (wrap-content-type)
      (wrap-head)))
