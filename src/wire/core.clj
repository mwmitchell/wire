(ns wire.core
  (:require [clojure.string :as s]
            [clojure.set :refer [difference]]))

(def http-methods #{:get :post :put :delete :patch :head :options :trace :connect})

(letfn [(find-by [pred x]
          (some #(and (pred %) %) x))]
  (defn- parse-opts
    "Accepts a variable typed input and returns an options map"
    [in]
    (cond
     (map? in) in
     (keyword? in) {:id in :path (name in)}
     (coll? in) (let [id (find-by keyword? in)]
                  {:id id
                   :path (or (find-by string? in) (when id (name id)))
                   :rules (find-by map? in)
                   :pre (find-by vector? in)})
     :else {})))

(defn- method? [v] (-> v (meta) :method))

(defn- route? [v] (-> v (meta) :route))

(defn handle [method handler]
  {:pre [(or (nil? method) (http-methods method))
         (ifn? handler)]}
  ^:method {method handler})

(defn- fetch-opts [v]
  (let [opts (filter #(and (not (method? %)) (not (route? %))) v)]
    (when (> (count opts) 1)
      (throw (Exception. "Route options must be a single value (keyword,vector or map)")))
    (parse-opts (first opts))))

(defn- base-route [parsed-opts & data]
  {:pre [(contains? parsed-opts :id)]}
  (-> parsed-opts
      (assoc :methods (apply merge (filter method? data)))
      (assoc :routes (filter route? data))
      (with-meta {:route true})))

(defn root [& data]
  (let [opts (fetch-opts data)]
    (apply base-route (merge {:path "" :id nil} opts) data)))

(defn route [& data]
  (let [opts (fetch-opts data)]
    (apply base-route opts data)))

(defmacro ^:private mk-method-route-fn [n m]
  `(defn ~n [fun#] (handle ~m fun#)))

;; Create the GET, PUT, POST etc. handlers
(doseq [n http-methods]
  (eval `(mk-method-route-fn ~(-> n name s/upper-case symbol) ~n)))

;; Special case for ANY
(mk-method-route-fn ANY nil)
