(ns wire.helpers
  (:require [wire.routing :as r]
            [wire.middleware :as mw]))

(defmacro defhelper [name local-name args & body]
  `(defn ~name
     ([request# ~@args]
        {:pre [(map? request#)]}
        (let [~local-name (mw/context request#)]
          ~@body))
     ([~@args]
        (let [~local-name (mw/context)]
          ~@body))))

;; Returns the root route from the context
(defhelper root c []
  (-> c :routes first))

;; Returns the route that matched the request
(defhelper current c []
  (-> c :routes last))

(defhelper parent c []
  (-> c :routes butlast last))

(defhelper depth c []
  (-> c :routes count))

(defhelper route-at c [depth]
  (get (-> c :routes) (- depth 1)))

(defhelper route-from c [inv-depth]
  (let [r {mw/match-id c}]
    (route-at r (- (depth r) inv-depth))))

(defhelper path c []
  (:path c))

(defhelper ids c []
  (:ids c))

(defhelper method c []
  (:method c))

(defhelper params c []
  (:params c))

;; Using the root route, builds a path based on the ids vector.
;; Params is a map of values for the path params.
(defhelper path-for c [ids params]
  (let [r {mw/match-id c}]
    (r/route-path (root r) ids params)))
