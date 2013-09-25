(ns wire.dispatch
  (:require [clout.core :as clout]
            [clojure.string :as s]))

(defn pre-match? [{fns :pre} request]
  (every? (fn [f] (f request)) fns))

(defn- method-matches? [method request]
  (or (nil? method)
      (let [r-method (:request-method request)
            form-method (-> request :form-params :_method)]
        (if (and form-method (= r-method :post))
          (= (s/upper-case (name form-method)) (s/upper-case (name method)))
          (= method r-method)))))

(defn path-matches? [{matcher :matcher :as route-def} request]
  {:pre [(not (nil? matcher))]}
  (when-let [params (clout/route-matches matcher request)]
    [route-def params]))

(defn dispatch [route-defs request]
  {:pre [(sequential? route-defs)]}
  (some #(and (method-matches? (:method %) request)
              (pre-match? % request)
              (path-matches? % request)) route-defs))
