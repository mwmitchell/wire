(ns wire.middleware
  (:require [wire.dispatch :refer [dispatch]]))

;; The name of the route-def that's injected into the request map
(def match-id :matched-route)

(defn exec-matched-route
  "The last element in the middleware chain.
   This executes the matched route handler"
  [r]
  (when (match-id r)
    ((-> r match-id :handler)
     (dissoc r match-id))))

(defn wrap-match
  "Injects the matched route and its path params into the request,
   then calls the next handler"
  [h routes]
  (fn [r]
    (when-let [match (dispatch routes r)]
      (let [[route-def path-params] match]
        (h (-> r
               (assoc-in [match-id] route-def)
               (update-in [:params] merge path-params)
               (assoc-in [:route-params] path-params)))))))

(defn wrap-not-found
  "Renders a 404 if a matched route was not found"
  [h text]
  (fn [r]
    (if-not (match-id r)
      {:body text :status 404}
      (h r))))
