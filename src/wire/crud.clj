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
      {:path [(str (name plural) "/:id/edit") :id #"[0-9]+"]
       :get (:edit fn-mapping)}]
     [plural
      {:path (name plural)
       :get (:index fn-mapping)
       :post (:create fn-mapping)}]]))


(comment
  (require 'wire.compile)
  (require 'wire.routing)
  (let [handlers {:create identity
                  :index identity
                  :update identity
                  :destroy identity
                  :new identity
                  :show identity
                  :edit identity}
        place-routes (resources :place :places handlers)
        routes (apply wire.routing/root {:path "admin"} place-routes)
        croutes (wire.compile/compile-route routes)
        match (some #((:matcher %) {:path-info "/admin/places/100/edit" :request-method :get}) croutes)]
    ((:handler match) {:params (:params match) :names (:names match)})))
