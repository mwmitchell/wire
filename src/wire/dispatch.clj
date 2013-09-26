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

(defn path-matches? [{matcher :path-fn :as route-def} request]
  {:pre [(not (nil? matcher))]}
  (when-let [params (clout/route-matches matcher request)]
    [route-def params]))

(defn dispatch [route-defs request]
  {:pre [(sequential? route-defs)]}
  (some #(and (method-matches? (:method %) request)
              (:pre-fn request)
              (path-matches? % request)) route-defs))
