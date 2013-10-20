(ns wire.example
  (:require [wire.helpers :as h]
            [wire.routing :as r]
            [wire.middleware :as m]
            [clojure.string :as s]))

(defn demo-response-handler
  [request]
  {:ids (h/ids request)
   :matched-method (h/method request)
   :full-route-path (h/path request)
   :url (h/path-for request (h/ids request) (h/params request))
   :parent-url (h/path-for request (butlast (h/ids request)) (h/params request))
   :params (h/params request)})

(defn redirect-to [request ids params]
  {:headers {"Location" (h/path-for request ids params)}
   :status 302})

(def app-routes
  (r/root
   {:any (fn [r] (redirect-to r [:login] {}))}
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
(r/route-path-by app-routes #(= (r/id %) :new-location) {})

;; path by name hierarchy:
(r/route-path app-routes [:admin :locations :location] {:id 1})

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
