(ns wire.navigation)

(defn collect-routes
  "Given a single route map, traverses and collects
   child routes whose :id matches the current value in `ids`
   Example:
   (collect-routes {:id nil :routes [{:id :sub}]} [:sub])"
  [root ids]
  (->> ids
       (reduce
        (fn [[{r :routes :as current} mem :as state] id]
          (when-let [child (->> r (filter #(= id (:id %))) first)]
            [child (conj mem child)]))
        [root [root]])
       (last)))

(defn find-route [root ids]
  (last (collect-routes root ids)))

(defn up
  "Removes an `n` number of values from the end of `ids`.
   Useful for relative/upward path navigation. Example: (route-path
   root (up my-ids 1) {})"
  [ids n]
  {:pre [(pos? n)]}
  (take (- (count ids) n) ids))
