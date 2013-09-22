(ns wire.routes
  (:require [clojure.string :as s]
            [clojure.set :as set]
            [clout.core :as clout]))

(def ^:dynamic *context* {})

(defn- get-path [x]
  (if (vector? x)
    (first x)
    x))

(defn- get-rules [x]
  (if (and (vector? x) (seq (rest x)))
    (apply hash-map (rest x))
    {}))

(defn- parse-context-args
  [arg]
  (cond
   (string? arg) {:path arg :rules {}}
   (vector? arg) {:path (get-path arg) :rules (get-rules arg)}
   (map? arg) (merge arg (parse-context-args (:path arg)))
   (keyword? arg) {:name arg}
   :else nil))

(defn- parse-route-args [args]
  {:pre [(fn? (last args))]}
  (assoc (parse-context-args (first args))
    :handler (last args)))

(let [accums {:pre concat
              :path str
              :rules merge}]
  (defn merge-context [old new]
    (reduce-kv
     (fn [m k v]
       (if-let [f (k accums)]
         (assoc m k (f (k m) v))
         (assoc m k v)))
     old
     new)))

(defn- mk-route-def
  [method & args]
  (assoc (merge-context *context* (parse-route-args args))
    :method method))

(defn GET [& args]
  (apply mk-route-def :get args))

(defn PUT [& args]
  (apply mk-route-def :put args))

(defn POST [& args]
  (apply mk-route-def :post args))

(defn DELETE [& args]
  (apply mk-route-def :delete args))

(defn PATCH [& args]
  (apply mk-route-def :patch args))

(defn ANY [& args]
  (apply mk-route-def nil args))

(defn context [spec & routes]
  (binding [*context* (merge-context *context* (parse-context-args spec))]
    (mapv #(merge-context *context* %) (flatten routes))))

(defn compile-route [route]
  {:pre [(map? route) (string? (:path route))]}
  (assoc route :matcher (clout/route-compile
                         (:path route)
                         (or (:rules route) {}))))

(defn compile-routes
  "Concatenates/compiles the route-defs and sorts by the longest (most specific) :path first"
  [& route-defs]
  (map compile-route
       (sort-by #(count (:path %)) > (apply concat route-defs))))
