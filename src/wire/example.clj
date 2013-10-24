(ns wire.example
  (:require [wire.bound-helpers :as h]
            [wire.routing :as r]
            [wire.middleware :as m]
            [clojure.string :as s]))

(defn demo-response-handler
  [_]
  {:depth (h/depth)
   :grand-parent (r/id (h/route-from 2))
   ;;:parent (r/id (h/parent request))
   :children (map r/id (r/children (h/current)))
   :ids (h/ids)
   :matched-method (h/method)
   :full-route-path (h/path)
   :url (h/path-for (h/ids) (h/params))
   :parent-url (h/path-for (butlast (h/ids)) (h/params))
   :params (h/params)})

(defn redirect-to [ids params]
  {:headers {"Location" (h/path-for ids params)}
   :status 302})

(def app-routes
  (r/root
   {:any (fn [r] (redirect-to [:login] {}))}
   [:login {:path "login.html" :get (fn [_] :about)}]
   [:admin {}
    [:locations {:get (fn [_] :locations)
                 :post (fn [_] :create)}
     [:new-location {:path "new" :get (fn [_] :new)}]
     [:location {:path ":id"
                 :get demo-response-handler
                 :put (fn [_] :update)
                 :delete (fn [_] :destroy)}
      [:clone {:post (fn [rq] (str (-> rq :params :id) " cloned!"))}]]]]))

(def app
  (-> m/wrap-exec-route
      (m/wrap-identify-route app-routes)))

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
 :clone-location-result
 (app {:path-info "/admin/locations/100/clone"
       :request-method :post})
 :demo-response
 (app {:path-info "/admin/locations/99"
       :request-method :get})}
