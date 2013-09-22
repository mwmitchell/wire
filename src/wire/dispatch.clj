(ns wire.dispatch
  (:require [clout.core :as clout]
            [clojure.string :as s]))

(defn- method-matches? [method request]
  (or (nil? method)
      (let [r-method (:request-method request)
            form-method (-> request :form-params :_method)]
        (if (and form-method (= r-method :post))
          (= (s/upper-case (name form-method)) (s/upper-case (name method)))
          (= method r-method)))))

(defn pre-match? [route-def request]
  (every? (fn [f] (f request)) (:pre route-def)))

(defn match-route [route-def request]
  (when-let [params (clout/route-matches (:matcher route-def) request)]
    [route-def params]) )

(defn dispatch [route-defs request]
  (some #(and (pre-match? % request)
              (method-matches? (:method %) request)
              (match-route % request)) route-defs))
