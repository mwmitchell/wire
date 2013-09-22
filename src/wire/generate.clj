(ns wire.generate
  (:require [clojure.string :as s]
            [clojure.set :as set]))

(defn find-route-def-by-name [route-defs rname]
  (or (some #(and (= (:name %) rname) %) route-defs)
      (throw (Exception. (format "Route not found: %s" rname)))))

(defn path-for [route-defs r-name opts]
  (let [{:keys [path]} (find-route-def-by-name route-defs r-name)
        pkeys (map (comp keyword last) (re-seq #":([^ \/]+)" path))
        diff (seq (set/difference (set pkeys) (-> opts keys set)))]
    (when diff
      (throw (Exception. (format "The following params are missing: %s" diff))))
    (reduce (fn [m [k v]] (s/replace m (str "/" k) (str "/" v))) path opts)))
