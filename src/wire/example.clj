(ns wire.example
  (:require [wire.core :refer :all]
            [wire.middleware :as m]
            [clojure.string :as s]
            [wire.navigation :as n]
            [wire.path :as p]))

(declare app-routes action-fn)

(defn action-fn [{route-context :route-context}]
  (let [routes (n/collect-routes app-routes (:ids route-context))]
    {:matched-method (:method route-context)
     :ids (:ids route-context)
     :full-route-path (s/join "/" (map :path routes))
     :url (p/route-path app-routes (:ids route-context) (:params route-context))
     :parent-url (p/route-path app-routes (n/up (:ids route-context) 1) (:params route-context))
     :params (get-in route-context [:params])}))

(def app-routes
  (root
   (GET (fn [_] :root))
   (route :sites
          (GET action-fn)
          (route [:site ":site" {:site #"[^/]+"}]
                 (GET action-fn)
                 (route :partners
                        (GET action-fn)
                        (DELETE action-fn)
                        (route [":id" :partner]
                               (GET action-fn)))))))

(def app
  (-> m/wrap-exec-route
      (m/wrap-identify-route app-routes)))

(let [request {:path-info "/sites/100/partners"
               :request-method :delete}]
  (app request))
