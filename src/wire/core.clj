(ns wire.core
  (:require [clojure.string :as s]
            [clojure.set :refer [difference]]))

(def http-methods #{:get :post :put :delete :patch :head :options :trace :connect})

(defn- parse-path [{v :path}]
  (cond
   (string? v) {:path v :rules {}}
   (vector? v) {:path (first v) :rules (apply hash-map (rest v))}
   :else (throw (Exception. "Path must be a string or vector"))))

(defn root
  "Creates a root container and sets path to an empty string"
  [opts & children]
  {:pre [(map? opts)]}
  `[nil ~(merge {:path ""} opts) ~@children])

(defn routes
  "Builds a tree of routes from:
   [:name opts-map child ...]"
  [[id opts & children]]
  (let [r (merge {:name id
                  :path (if id (name id) nil)
                  :methods (select-keys opts (conj http-methods :any))
                  :routes (map routes children)}
                 (apply dissoc opts http-methods))]
    (merge r (parse-path r))))
