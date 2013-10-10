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
  (routes
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

A route's path/rules must be compiled in order to dispatch:

```clojure
(wire.compile/compile-route my-routes)
```

Compiling the route creates a collection of route dispatch functions. The dispatch function argument is a standard Ring request.
If the route matches the request, a map is returned:

```clojure
(def compiled-routes (wire.compile/compile-route my-routes))
(def result (some #(% {:path-info "/admin/locations/new" :request-method :get}) compiled-routes))
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
(routes [:parent-name {:get my-handler}
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
