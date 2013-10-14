(ns wire.compile
  (:require [clojure.string :as s]
            [clout.core :as clout]))

(defn- request-method
  "Accepts a Ring request map, returns a request method (keyword).
   If [:form-params :_method] is present, AND the :request-method is :post
   the :_method value is used as a psuedo request method."
  [request]
  (let [r-method (:request-method request)
        form-method (-> request :form-params :_method)]
    (if (and form-method (= r-method :post))
      (keyword (s/lower-case (name form-method)))
      r-method)))

(defn compile-route
  "Accepts a route and returns a vector of dispatch functions.
   The tree structure of the given route (:children)
   is recursed to create the flat output vector.
   Each dispatch function requires a Ring request map.
   A matching dispatcher (when executed) will return a map:
     :route - the matching route map
     :handler - the matching route handler function
     :method - the matching request method
     :path - the full path to the route
     :names - a vector of :name values from the root down to the matching route"
  [{methods :methods :as route} & [parents]]
  (let [rules (merge (apply merge (map :rules parents)) (:rules route))
        names (vec (keep identity (conj (mapv :name parents) (:name route))))
        path-components (conj (->> parents (map :path) (keep not-empty) (vec)) (:path route))
        full-path (str "/" (s/join "/" path-components))
        path-matcher (clout/route-compile full-path rules)
        matcher (fn [r]
                  (let [rmethod (request-method r)]
                    (when-let [mm (or (and (contains? methods rmethod) rmethod)
                                      (and (contains? methods :any) :any))]
                      (when-let [path-params (clout/route-matches path-matcher r)]
                        {:route route
                         :handler (get-in route [:methods mm])
                         :method mm
                         :params path-params
                         :path full-path
                         :names names}))))]
    (reduce
     (fn [mem r]
       (concat mem (compile-route r (conj (vec parents) route))))
     [matcher]
     (:children route))))
