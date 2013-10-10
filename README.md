# Wire

Simple routing for Ring.

The goals of Wire are:

  * define and represent routes as tree structure
  * route context and metadata support
  * simple helpers (path generation etc.)
  * support standard Ring interface
  * separate identification-of route and execution-of matched handler

## Usage
Leiningen TODO...
[codesignals/wire "0.2.0"]

## Description

Here's a sample route definition:

```clojure
(def my-routes
  {:name :parent
   :methods {:get (fn [request])}
   :path "items"
   :routes [{:name :child
             :path ":id"
             :rules {:id #"[0-9]+"}
             :methods {:get (fn [request])}}]})
```

A route's path/rules must be compiled in order to dispatch:

```clojure
(wire.compile/compile-route my-routes)
```

Compiling the route creates a collection of route dispatch functions. The dispatch function argument is a standard Ring request.
If the route matches the request, a map is returned:

```clojure
(def compiled-routes (wire.compile/compile-route my-routes))
(def result (some #(% {:path-info "1" :request-method :get}) compiled-routes))
```

`result` would end up looking something like:

```clojure
{:route ...the route that matched the request
 :handler .. the matching request method handler function
 :method .. the request method that matched
 :params .. route path param values
 :path ... the full, matching route path
 :names ... a vector path of route names}
```

### Helpers

#### Routes
Wire provides a few helpers for building routes:

```clojure
(routes [:parent-name {:path "xyz.html" :get my-handler}
         [:child-name {} ...]])
```

##### Arguments...
TODO...

#### Paths
TODO...

#### Navigation
TODO...

See the example.clj file for an example.

## License

Copyright © 2013 Matt Mitchell

Distributed under the Eclipse Public License, the same as Clojure.
