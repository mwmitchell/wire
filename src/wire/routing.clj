(ns wire.routing
  (:require [clojure.set :refer [difference]]
            [clojure.string :as s]))

(def ^:private http-methods #{:get :post :put :delete :patch :head :options :trace :connect})

(def ^:private valid-method-names (conj http-methods :any))

(defn attrs [[id a & _]]
  (assoc a :id id))

(defn handlers [[_ a & _]]
  (select-keys a valid-method-names))

(defn children [[_ _ & children]]
  children)

(defn id [[id & _]] id)

(defn path [[id {p :path} & _]]
  (or (when (vector? p) (first p))
      p
      (when (string? id) id)
      (s/join #"/" (keep #(% id) [namespace name]))))

(defn rules [[_ {p :path r :rules} & _]]
  (or
   (when (vector? p) (apply hash-map (rest p)))
   r
   {}))

(defn collect-by
  "Returns a route (and ancestors) if pred returns true."
  [route pred & [parents]]
  (if (pred route)
    (conj parents route)
    (some #(collect-by % pred (conj (vec parents) %)) (children route))))

(defn collect-by-id
  "Returns a route (and ancestors) if the route-id matches"
  [route route-id]
  (collect-by route #(= (id %) route-id)))

(defn collect-each
  "Returns routes matching the path of `names`"
  [root names]
  (->> names
       (reduce
        (fn [[current mem] rname]
          (when-let [child (->> (children current) (filter #(= rname (id %))) first)]
            [child (conj mem child)]))
        [root [root]])
       (last)))

(defn root
  "Builds a default root route w/an empty path"
  [opts & children]
  (vec (concat [nil (merge {:path ""} opts)] children)))

;; Path functions

(defn- replace-wildcards [p wildcard-values]
  (if-let [pkeys (seq (mapv (comp keyword last) (re-seq #"(\*)" p)))]
    (do (when-not (= (count wildcard-values) (count pkeys))
          (throw (Exception. (format "The route wildcards for %s are missing" p))))
        (reduce #(s/replace-first %1 "*" (str %2)) p wildcard-values))
    p))

(defn- replace-named-params
  [p opts]
  (let [pkeys (mapv (comp keyword last) (re-seq #":([^ \/\?\&\.]+)" p))]
    (when-let [diff (seq (difference (set pkeys) (-> opts keys set)))]
      (throw (Exception. (format "The route params %s for %s are missing" diff p))))
    (reduce-kv #(s/replace %1 (str ":" (name %2)) (str %3)) p opts)))

(defn- replace-params [p opts]
  (replace-wildcards
   (replace-named-params p (dissoc opts :*))
   (:* opts)))

(defn- make-path [route-set path-values]
  (str "/"
       (replace-params
        (->> route-set
             (keep (comp not-empty path))
             (s/join "/"))
        path-values)))

(defn route-path
  "Creates a param populated path suitable for use in an URL.
   Example:
   (route-path route [:sub-of-root :sub-of-sub] {:param 100})"
  [root names path-values]
  (make-path (collect-each root names) path-values))

(defn route-path-by [route fn path-values]
  (make-path (butlast (collect-by route fn)) path-values))
