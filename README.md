# Wire

Simple routing for Ring.

The goals of Wire are:

  * define and represent routes as tree structure
  * route context and metadata support
  * simple helpers (path generation etc.)
  * support standard Ring interface
  * separate identification-of route and execution-of matched handler

##Examples
There's an example app in the "/wireapp" directory, and an example.clj file in /src/wire.

## Usage

```clojure
[codesignals/wire "0.3.0"]
```

## Description

Here's a sample route definition:

```clojure
(require '[wire.routing :refer :all])

(def my-routes
  (root
   [:login {:path "login.html"
            :get show-login
            :post do-login}]
   [:admin {}
    [:locations {:get list-locations
                 :post create-location}
     [:new-location {:path "new" :get show-location-form}]
     [:location {:path [":id" :id #"[0-9]+"]
                 :get show-location
                 :put update-location
                 :delete destroy-location}]]])
```

The main structure that contains a route is a vector. The first element in the vector is the route's ID. This ID is an identifier used for locating the route and building paths etc..

The next element is a hash-map describing the route options; The request method handlers, the path, etc..

Following the options hash-map is the list of child routes.

```clojure
[:id {:get (fn [request])}
  [:child-a-id {:get (fn [request])}
    [:child-a-a-id {:path ["a-a.:format" :format #"[a-z]{3,0}"]
                    :get (fn [request])}]]
  [:child-b {:path "b" ...}]]
```

##API

The wire.routing namespace contains functions for building, working-with and building paths for route vectors.

##Compiling

A route's path/rules must be compiled in order to dispatch:

```clojure
(wire.compile/compile-route my-routes)
```

Compiling the route creates a collection of route dispatch functions. The dispatch function argument is a standard Ring request.
If the route matches the request, a map is returned:

```clojure
(def compiled-routes (wire.compile/compile-route my-routes))
(def result (some #((:matcher %) {:path-info "/admin/locations/new" :request-method :get}) compiled-routes))
```

`result` would end up looking something like:

```clojure
;; TODO...
```

### Helpers

#### Routes
Wire provides a few helpers for building routes:

```clojure
(root {}
  [:parent-name {:get my-handler}
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
