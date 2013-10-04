(ns wire.navigation)

(defn collect-routes
  "Given a single route map, traverses and collects
   child routes whose :id matches the current value in `ids`
   Example:
   (collect-routes {:name nil :routes [{:name :sub}]} [:sub])"
  [root names]
  (->> names
       (reduce
        (fn [[{r :routes :as current} mem :as state] rname]
          (when-let [child (->> r (filter #(= rname (:name %))) first)]
            [child (conj mem child)]))
        [root [root]])
       (last)))

(defn find-route [root names]
  (last (collect-routes root names)))

;; TODO: convert to reduce...
(defn collect-by-id
  "Finds a route (and parents) by (assumed to be globally unique) :id"
  [route id & [parents]]
  (if (= (:id route) id)
    (conj (vec parents) route)
    (some #(collect-by-id % id (vec (conj parents route))) (:routes route))))

(defn find-by-id [route id]
  (last (collect-by-id route id)))

(defn up
  "Removes an `n` number of values from the end of `ids`.
   Useful for relative/upward path navigation. Example: (route-path
   root (up my-ids 1) {})"
  [names n]
  {:pre [(pos? n)]}
  (take (- (count names) n) names))
