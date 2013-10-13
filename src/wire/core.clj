(ns wire.core
  (:require [clojure.string :as s]
            [clojure.set :refer [difference]]))

;; A Routes is a map:
;; {:name :keyword-identifier
;;  :path "the-path" ;; also ["path/:var" :var #"regexp-rule"]
;;  :methods {:get fn1 :post fn2}
;;  :children []}

(def http-methods #{:get :post :put :delete :patch :head :options :trace :connect})

(defn- merge-path-attrs [{v :path :as m}]
  (merge m (cond
            (string? v) {:path v :rules {}}
            (vector? v) {:path (first v) :rules (apply hash-map (rest v))}
            :else (throw (Exception. "Path must be a string or vector")))))

(defn route
  "Builds a tree of routes from:
   [:name opts-map child1 child2 child3 ...]
  If a :path is not provided, the id of the route is used."
  [id opts & children]
  {:pre [(map? opts)]}
  (-> {:name id
       :path (when id (name id))
       :methods (select-keys opts (conj http-methods :any))
       :children (mapv #(apply route %) children)}
      (merge (apply dissoc opts http-methods))
      (merge-path-attrs)))

(defn routes
  "Builds a set of routes with a base root node"
  [root-opts & children]
  (apply route nil (merge {:path ""} root-opts) children))
