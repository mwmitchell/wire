# Wire

Simple routing for Ring.

The goals of Wire are:

  * define and represent routes as tree structure
  * route context and metadata support
  * simple helpers (path generation etc.)
  * support standard Ring interface
  * separate identification-of route and execution-of matched handler

##Examples
There's an example app in the [wireapp](https://github.com/mwmitchell/wire/tree/master/wireapp) directory, and an [example.clj](https://github.com/mwmitchell/wire/blob/master/src/wire/example.clj) file.

## Usage

```clojure
[codesignals/wire "0.4.1"]
```

## Description
A vector contains the definition of a route. The first element in the vector is the route's ID. This ID is an identifier used for locating the route and building paths etc..

The next element is a hash-map describing the route options; The request method handlers, the path, etc..

Following the options hash-map is the list of child routes.

Here's a sample route definition:

```clojure
(require '[wire.routing :refer :all])

(def my-routes
  [nil {}
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
                 :delete destroy-location}]]]])
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
###CRUD
Wire features a simple route builder for Rails-like CRUD apps, via `wire.crud`.
The wire.crud/resources function accepts the singular and plural forms of the resource name,
and a map of functions to handle the supported actions; :create,:index,:update,:destroy,:new,:show,:edit.

Here's a small example, which simple uses `identity` on the request for each action handler:

```clojure
(require 'wire.compile)
(require 'wire.routing)
(require 'wire.crud)
(let [handlers {:create identity
                :index identity
                :update identity
                :destroy identity
                :new identity
                :show identity
                :edit identity}
      place-routes (wire.crud/resources :place :places handlers)
      ;; Wire routes are easily composable!
      ;; Here, we're adding /admin to each place route
      routes (apply wire.routing/root {:path "admin"} place-routes)
      http-request {:path-info "/admin/places/100/edit" :request-method :get}
      match ((wire.compile/identifier routes) http-request)]
  ((:handler match) {:path (:path match)
                     :rules (:rules match)
                     :params (:params match)
                     :ids (:ids match)}))
```

###Middleware

```clojure
(require '[wire.middleware :as m])

(def handler
  (-> m/wrap-exec-route
      (m/wrap-identify-route app-routes)))
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
