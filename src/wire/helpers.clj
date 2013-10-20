(ns wire.helpers
  (:require [wire.routing :as r]
            [wire.middleware :as mw]))

(defn context
  "Returns the matched route context map"
  [request]
  (mw/match-id request))

(defn root
  "Returns the root route from the context"
  [request]
  (-> request context :routes (first)))

(defn current
  "Returns the route that matched the request"
  [request]
  (-> request context :routes (last)))

(defn path [request]
  (-> request context :path))

(defn ids [request]
  (-> request context :ids))

(defn method [request]
  (-> request context :method))

(defn params [request]
  (-> request context :params))

(defn path-for
  "Using the root route, builds a path based on the ids vector.
   Params is a map of values for the path params."
  [request ids & [params]]
  (r/route-path (root request) ids params))
