(ns wire.bound-helpers
  (:require [wire.middleware :as mw]
            [wire.helpers]))

(defmacro defhelper [name v]
  `(defn ~name [& args#]
     (apply ~v {mw/match-id mw/*context*} args#)))

(doseq [[k v] (ns-publics 'wire.helpers)]
  (eval `(defhelper ~k ~v)))
