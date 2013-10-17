(ns wire.middleware
  (require [wire.compile :as compile]))

;; The name of the route-def that's injected into the request map
(def match-id :route-context)

(defn wrap-exec-route
  "The last element in the middleware chain.
   This executes the matched route handler"
  [r]
  (when-let [handler (get-in r [match-id :handler])]
    (handler r)))

(defn wrap-identify-route
  "Injects the matched route and its path params into the request,
   then calls the next handler."
  [h routes]
  (let [compiled-routes (compile/compile-route routes)]
    (fn [request]
      (when-let [match (some #(% request) compiled-routes)]
        (h (-> request
               (assoc-in [match-id] match)
               (update-in [:params] merge (:params match))))) )))

(defn wrap-not-found
  "Renders a 404 if a matched route was not found"
  [h text]
  (fn [r]
    (if-not (match-id r)
      {:body text :status 404}
      (h r))))
