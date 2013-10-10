(ns wire.core
  (:require [clojure.string :as s]
            [clojure.set :refer [difference]]))

(def http-methods #{:get :post :put :delete :patch :head :options :trace :connect})

(defn- parse-path [{v :path}]
  (cond
   (string? v) {:path v :rules {}}
   (vector? v) {:path (first v) :rules (apply hash-map (rest v))}
   :else (throw (Exception. "Path must be a string or vector"))))

(defn route
  "Builds a tree of routes from:
   [:name opts-map child1 child2 child3 ...]"
  [id opts & children]
  {:pre [(map? opts)]}
  (let [r (merge {:name id
                  :path (when id (name id))
                  :methods (select-keys opts (conj http-methods :any))
                  :routes (mapv #(apply route %) children)}
                 (apply dissoc opts http-methods))]
    (merge r (parse-path r))))

(defn routes
  "Builds a set of routes with a base root node"
  [root-opts & children]
  (apply route nil (merge {:path ""} root-opts) children))
