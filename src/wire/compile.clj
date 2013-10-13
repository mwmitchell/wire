(ns wire.compile
  (:require [clojure.string :as s]
            [clout.core :as clout]))

(defn- request-method [request]
  (let [r-method (:request-method request)
        form-method (-> request :form-params :_method)]
    (if (and form-method (= r-method :post))
      (keyword (s/lower-case (name form-method)))
      r-method)))

(defn compile-route [{methods :methods :as route} & [parents]]
  (let [rules (merge {} (apply merge (map :rules parents)) (:rules route))
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
