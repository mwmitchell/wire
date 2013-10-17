(ns wireapp.core
  (:require [wire.routing :as r]
            [wire.middleware :as m]
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

(declare app-routes)

(defn path-for [names & {:as params}]
  {:pre [(vector? names)]}
  (r/route-path app-routes names params))

(defn render [body & {:keys [status headers]}]
  {:body body :status (or status 200) :headers (or headers {})})

(defn render-html [body & {:keys [status headers]}]
  (render body :status status :headers (merge headers {"Content-Type" "text/html"})))

(defn navigation [current-route]
  (html/html [:ul
              [:li [:a {:href (path-for [])} "home"]]
              [:li [:a {:href (path-for [:about])} "about"]]
              [:li [:a {:href (path-for [:categories])} "categories"]]
              [:li [:a {:href (path-for [:categories :one] :format "csv")} "category I"]]
              [:li [:a {:href (path-for [:categories :two] :format "pdf")} "category II"]]
              [:li [:a {:href (path-for [:resources] :* ["app.css"])} "app.css"]]]))

(defn page [title content]
  (render-html
   (page/html5
    (page/include-css (str (path-for []) "app.css"))
    [:head [:title title]]
    [:body [:div {:id "container"}
            [:div {:id "left"} (navigation nil)]
            [:div {:id "right"}
             [:p [:h3 title]
              content]]]])))

(def app-routes
  (r/root
   ;; Root handlers...
   {:get (fn [_] (page "Wire Example" (path-for [])))}
   ;; Child routes...
   [:about {:get (fn [_] (page "About" "Hi there."))}]
   [:categories {:get (fn [{{route :route ids :ids} :route-context}]
                        (page "Categories"
                              (html/html
                               [:ul
                                (for [child (r/children route)]
                                  [:li [:a {:href (path-for (conj ids (r/id child)) :format "xml")} (r/id child)]])])))}
    [:one {::schema-validate? true
           ::basic-auth? true
           :path "I.:format"
           :get (fn [_] (page "one" "I"))}]
    [:two {:path "II.:format"
           :get (fn [_] (page "two" "II"))}]]
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
