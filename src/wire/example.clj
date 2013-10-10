(ns wire.example
  (:require [wire.core :as c]
            [wire.middleware :as m]
            [clojure.string :as s]
            [wire.navigation :as n]
            [wire.path :as p]))

(declare app-routes)

(defn demo-response-handler
  [{route-context :route-context}]
  (let [hierarchy (n/collect-routes app-routes (:names route-context))]
    {:matched-method (:method route-context)
     :names (:names route-context)
     :full-route-path (s/join "/" (map :path hierarchy))
     :url (p/route-path app-routes (:names route-context) (:params route-context))
     :parent-url (p/route-path app-routes (n/up (:names route-context) 1) (:params route-context))
     :params (get-in route-context [:params])}))

(defn redirect-to [routes path params]
  {:headers {"Location" (p/route-path app-routes path params)}
   :status 302})

(def app-routes
  (c/routes
   {:any (fn [_] (redirect-to app-routes [:login] {}))}
   [:login {:path "login.html" :get (fn [_] :about)}]
   [:admin {}
    [:locations {:get (fn [_] :locations)
                 :post (fn [_] :create)}
     [:new-location {:path "new" :get (fn [_] :new)}]
     [:location {:path ":id"
                 :get demo-response-handler
                 :put (fn [_] :update)
                 :delete (fn [_] :destroy)}]]]))

(def app
  (-> m/wrap-exec-route
      (m/wrap-identify-route app-routes)))

;; path by global id:
(p/route-path-by-id app-routes :new-location {})

;; path by name hierarchy:
(p/route-path app-routes [:admin :locations :location] {:id 1})

;; Dispatch...
{:root-result
 (app {:path-info "/"
       :request-method :head})
 :new-location-result
 (app {:path-info "/admin/locations"
       :request-method :post})
 :destroy-location-result
 (app {:path-info "/admin/locations/100"
       :request-method :delete})
 :demo-response
 (app {:path-info "/admin/locations/99"
       :request-method :get})}
