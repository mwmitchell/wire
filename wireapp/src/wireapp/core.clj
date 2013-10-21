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

(defn nav-item [rq label ids & [params]]
  [:li [:a {:href (path-for rq ids params)} label]])

(defn navigation [rq]
  (let [ni (partial nav-item rq)]
    (html/html
     [:ul
      (ni "home" [])
      (ni "about" [:about])
      (ni "categories" [:categories])
      (ni "category I" [:categories :one] {:format "csv"})
      (ni "category II" [:categories :two] {:format "pdf"})
      (ni "app.css" [:resources] {:* ["app.css"]})])))

(defn page [rq title content]
  (render-html
   (page/html5
    (page/include-css (str (path-for rq [:resources] {:* ["app.css"]})))
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
   ;; static asset handling
   [:resources (resources "public")]))

(defn wrap-route-logger [h]
  (fn [r]
    (log/infof "Wire route context: %s" (dissoc (h/context r) :routes))
    (h r)))

(def handler
  (-> m/wrap-exec-route
      (wrap-route-logger)
      (m/wrap-identify-route app-routes)
      (wrap-file-info)
      (wrap-content-type)
      (wrap-head)))
