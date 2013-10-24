(ns wire.routing-test
  (:require [wire.routing :refer :all]
            [midje.sweet :refer :all]))

(fact "attrs"
  (attrs [:the-id {:name "name"}])
  =>
  {:id :the-id :name "name"})

(fact "root"
  (root {}) => [nil {:path ""}])

(fact "collect-by for root"
  (let [routes [:root {:root? true}
                [:child-a {}
                 [:child-a-a {:foo :bar}]]]]
    (collect-by routes (comp :root? attrs))
    =>
    (contains (list routes))))

(fact "collect-by for sub-route"
  (let [routes [:root {}
                [:child-a {}
                 [:child-a-a {:foo :bar}]]]]
    (collect-by routes (comp :foo attrs))
    =>
    [[:root {} [:child-a {} [:child-a-a {:foo :bar}]]]
     [:child-a {} [:child-a-a {:foo :bar}]]
     [:child-a-a {:foo :bar}]]))

(fact "collect-each"
  (let [routes [:root {}
                [:child-a {}
                 [:child-a-a {}]]
                [:child-b {}
                 [:child-b-b {}]]]]
    (collect-by-each-id routes [:child-b :child-b-b])
    =>
    [[:root {} [:child-a {} [:child-a-a {}]] [:child-b {} [:child-b-b {}]]]
     [:child-b {} [:child-b-b {}]]
     [:child-b-b {}]]))

(fact "route-path"
  (let [routes [:root {}
                [:child-a {}
                 [:child-a-a {}]]
                [:child-b {}
                 [:child-b-b {}]]]]
    (route-path routes [:child-b :child-b-b] {})
    =>
    "/root/child-b/child-b-b"))

(fact "route-path with nil root id"
  (let [routes [nil {:path ""}
                [:child-a {}
                 [:child-a-a {}]]
                [:child-b {}
                 [:child-b-b {}]]]]
    (route-path routes [:child-b :child-b-b] {})
    =>
    "/child-b/child-b-b"))

(fact "route-path with path param"
  (let [routes [nil {:path ""}
                [:child-a {}
                 [:child-a-a {}]]
                [:child-b {:path ":foo"}
                 [:child-b-b {}]]]]
    (route-path routes [:child-b :child-b-b] {:foo "FOO!"})
    =>
    "/FOO!/child-b-b"))

(fact "route-path with wildcards"
  (let [routes [nil {:path ""}
                [:child-a {}
                 [:child-a-a {}]]
                [:child-b {:path "*"}
                 [:child-b-b {:path "*"}]]]]
    (route-path routes [:child-b :child-b-b] {:* ["foo" "bar"]})
    =>
    "/foo/bar"))
