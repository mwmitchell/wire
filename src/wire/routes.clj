(ns wire.routes
  (:require [clojure.string :as s]
            [clojure.set :as set]
            [clout.core :as clout]))

(defrecord Route [name path rules method pre handler handler-fn ])

(def ^:dynamic *context* {})

(defn- get-path [x]
  (when (vector? x)
    (first x)))

(defn- get-rules [x]
  (when (and (vector? x) (seq (rest x)))
    (apply hash-map (rest x))))

(defn- parse-context-args
  [arg]
  (cond
   (string? arg) {:path arg}
   (vector? arg) {:path (get-path arg) :rules (get-rules arg)}
   (keyword? arg) {:name arg}
   (map? arg) (merge arg (parse-context-args (:path arg)))
   :else nil))

(defn- parse-route-args [args]
  {:pre [(or (ifn? (last args)) (symbol? (last args)))]}
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

(defn mk-route
  [method & args]
  (assoc (merge-context *context* (parse-route-args args))
    :method method))

(defmacro ^:private mk-method-route-fn [n m]
  `(defn ~n [& args#] (apply mk-route ~m args#)))

(doseq [n [:get :post :put :delete :patch :head :options :trace :connect]]
  (eval `(mk-method-route-fn ~(-> n name s/upper-case symbol) ~n)))

(mk-method-route-fn ANY nil)

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
