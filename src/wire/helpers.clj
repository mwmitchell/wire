(ns wire.helpers
  (:require [wire.routing :as rt]
            [wire.middleware :as mw]))

(defn- context [r] (mw/context r))

(defn routes [r]
  (-> r context :routes))

(defn path [r]
  (-> r context :path ))

(defn ids [r]
  (-> r context :ids))

(defn method [r]
  (-> r context :method))

(defn params [r]
  (-> r context :params))

(defn root
  "Returns the root route from the context"
  [r]
  (first (routes r)))

(defn current
  "Returns the route that matched the request"
  [r]
  (last (routes r)))

(defn parent
  "Returns the parent of the matched route"
  [r]
  (-> (routes r) butlast last))

(defn depth
  "Returns the level of the matching route"
  [r]
  (count (routes r)))

(defn route-at
  "Returns the route at depth (1-based index)"
  [r depth]
  (get (routes r) (- depth 1)))

(defn route-from
  "Returns the route from the current, up inv-depth levels"
  [r inv-depth]
  (route-at r (- (depth r) inv-depth)))

;; Using the root route, builds a path based on the ids vector.
;; Params is a map of values for the path params.
(defn path-for
  "Returns a string path using wire.routing/route-path"
  [r ids params]
  (rt/route-path (root r) ids params))
