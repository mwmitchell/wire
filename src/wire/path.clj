(ns wire.path
  (:require [wire.navigation :refer [collect-routes collect-by-id]]
            [clojure.set :refer [difference]]
            [clojure.string :as s]))

(defn- replace-wildcards [path wildcard-values]
  (if-let [pkeys (seq (mapv (comp keyword last) (re-seq #"(\*)" path)))]
    (do
      (assert (coll? wildcard-values))
      (when-not (= (count wildcard-values) (count pkeys))
        (throw (Exception. (format "The route wildcards for %s are missing" path))))
      (reduce #(s/replace-first %1 "*" (str %2)) path wildcard-values))
    path))

(defn- replace-named-params
  [path opts]
  (let [pkeys (mapv (comp keyword last) (re-seq #":([^ \/\?\&]+)" path))]
    (when-let [diff (seq (difference (set pkeys) (-> opts keys set)))]
      (throw (Exception. (format "The route params %s for %s are missing" diff path))))
    (reduce-kv #(s/replace %1 (str ":" (name %2)) (str %3)) path opts)))

(defn- replace-params [path opts]
  (replace-wildcards
   (replace-named-params path (dissoc opts :*))
   (:* opts)))

;;

(defn- make-path [route-set path-values]
  (str "/"
       (replace-params
        (->> route-set
             (keep (comp not-empty :path))
             (s/join "/"))
        path-values)))

(defn route-path
  "Creates a param populated path suitable for use in an URL.
   Example:
   (route-path root-route [:child :node] {:param 100})"
  [root names path-values]
  (make-path (collect-routes root names) path-values))

(defn route-path-by-id [route id path-values]
  (make-path (collect-by-id route id) path-values))
