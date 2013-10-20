(ns wire.middleware
  (require [wire.compile :as c]
           [compojure.response :as r]))

;; The key of the route context that's injected into the request map
(def match-id ::route)

(defn wrap-exec-route
  "The last element in the middleware chain.
   This executes the matched route handler"
  [r]
  (when-let [handler (get-in r [match-id :handler])]
    (r/render (handler r) r)))

(defn wrap-identify-route
  "Injects the matched route and its path params into the request,
   then calls the next handler."
  [h route]
  (let [identifier (c/identifier route)]
    (fn [request]
      (let [match (identifier request)]
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
