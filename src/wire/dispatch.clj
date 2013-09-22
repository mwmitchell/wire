(ns wire.dispatch
  (:require [clout.core :as clout]
            [clojure.string :as s]))

(defn pre-match? [route-def request mapping]
  (every? (fn [pre]
            (if-let [f (pre mapping)]
              (f request)
              (pre request)))
          (:pre route-def)))

(defn- method-matches? [method request]
  (or (nil? method)
      (let [r-method (:request-method request)
            form-method (-> request :form-params :_method)]
        (if (and form-method (= r-method :post))
          (= (s/upper-case (name form-method)) (s/upper-case (name method)))
          (= method r-method)))))

(defn path-matches? [route-def request]
  (when-let [params (clout/route-matches (:matcher route-def) request)]
    [route-def params]))

(defn dispatch [route-defs request & [pre-mapping handler-mapping]]
  (when-let [[{handler :handler :as route} path-info]
             (some #(and (method-matches? (:method %) request)
                         (pre-match? % request pre-mapping)
                         (path-matches? % request)) route-defs)]
    [(assoc route :handler-fn (get handler-mapping handler handler))
     path-info]))
