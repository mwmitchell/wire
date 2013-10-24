(ns wire.crud)

(def actions #{:create :index :show :edit :update :destroy :new})

(defn ns-actions [ns]
  (let [pubs (ns-publics ns)]
    (reduce (fn [mem action]
              (let [aname (-> action name symbol)]
                (assoc mem action (get pubs aname)))) {} actions)))

(defn resources [singular plural action-source]
  {:pre [(or (map? action-source) (symbol? action-source))]}
  (let [fn-mapping (if (map? action-source)
                     action-source
                     (ns-actions action-source))]
    [[(keyword (str "new-" (name singular)))
      {:path (str (name plural) "/new")
       :get (:new fn-mapping)}]
     [singular
      {:path (str (name plural) "/:id")
       :get (:show fn-mapping)
       :put (:update fn-mapping)
       :delete (:destroy fn-mapping)}]
     [(keyword (str "edit-" (name singular)))
      {:path [(str (name plural) "/:id/edit")]
       :get (:edit fn-mapping)}]
     [plural
      {:path (name plural)
       :get (:index fn-mapping)
       :post (:create fn-mapping)}]]))
