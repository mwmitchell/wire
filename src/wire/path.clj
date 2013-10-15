(ns wire.path
  (:require [wire.navigation :refer [collect-routes collect-by-id]]
            [clojure.set :refer [difference]]
            [clojure.string :as s]))

(defn replace-params
  "Replaces the path params (fragments starting with a :)
   in `path` with the values in `opts`
   Example:
   (replace-params \"my/path/:id\" {:id 1})"
  [path opts]
  (let [pkeys (map (comp keyword last) (re-seq #":([^ \/\?\&]+)" path))]
    (when-let [diff (seq (difference (set pkeys) (-> opts keys set)))]
      (throw (Exception. (format "The route params %s for %s are missing" diff path))))
    (reduce-kv #(s/replace %1 (str ":" (name %2)) (str %3)) path opts)))

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
