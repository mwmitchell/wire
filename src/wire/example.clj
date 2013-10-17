(ns wire.example
  (:require [wire.routing :as r]
            [wire.middleware :as m]
            [clojure.string :as s]))

(declare app-routes)

(defn demo-response-handler
  [{route-context :route-context}]
  (let [hierarchy (r/collect-each app-routes (:ids route-context))]
    {:hierarchy (:ids route-context)
     :matched-method (:method route-context)
     :names (:ids route-context)
     :full-route-path (s/join "/" (map r/path hierarchy))
     :url (r/route-path app-routes (:ids route-context) (:params route-context))
     :parent-url (r/route-path app-routes (butlast (:ids route-context)) (:params route-context))
     :params (get-in route-context [:params])}))

(defn redirect-to [routes path params]
  {:headers {"Location" (r/route-path app-routes path params)}
   :status 302})

(def app-routes
  (r/root
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
