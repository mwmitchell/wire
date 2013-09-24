(ns wire.generate
  (:require [clojure.string :as s]
            [clojure.set :refer [difference]]))

(defn find-by-id [route-defs id]
  (or (some #(and (= (:id %) id) %) route-defs)
      (throw (Exception. (format "Route not found: %s" id)))))

(defn path-for [route-defs id opts]
  (let [{p :path} (find-by-id route-defs id)
        pkeys (map (comp keyword last) (re-seq #":([^ \/]+)" p))]
    (when-let [diff (seq (difference (set pkeys) (-> opts keys set)))]
      (throw (Exception. (format "The route params for %s are missing: %s" id diff))))
    (reduce-kv #(s/replace %1 (str "/" %2) (str "/" %3)) p opts)))
