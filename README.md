# Wire

Simple routing for Ring.

The goals of Wire are:

  * use data to describes routes (not wrapped functions)
  * route contexts
  * route metadata (they're just maps)
  * path generation
  * simple *function* helpers
  * support standard Ring interface
  * separate identification of route and execution of matched handler

## Usage

Route definitions are maps:

```clojure
(def my-route
  {:path "/:id"
   :rules {:id #"[0-9]+"}
   :name :item
   :method :get
   :handler (fn [request])
   :pre [pre-dispatch-predicates ...]})
```

A route can be compiled:

```clojure
(wire.routes/compile-route my-route)
```

Compiling the route simply adds a new key to the map called :matcher, which is a compiled Clout Route.

To match a compiled route and execute it:

```clojure
(let [[path-params matched-route] (wire.dispatch/dispatch [my-route] request)]
  ((:handler matched-route) (assoc request :path-params path-params)))
```

### Helpers

Wire provides a few helpers for building routes. The helpers include *functions* for buliding routes based off of a request method and for adding context. Here's a small example:

```clojure
(def app-routes
  (context "/"
    (GET my-handler)
    (GET "contact" render-contact)
    (GET [":page.html" :page #".+"] render-page)
    (context {:path "/admin" :pre [auth-check]}
      (GET "dashboard" render-admin-dashboard)
      (GET {:name :admin-stats
            :path "stats/:view"
            :rules {:view #".+"}
            :handler view-stats}))))
```

The result of which is a single-level vector of route map definitions:
```clojure
[{:path "/admin/dashboard" :handler [render-admin-dashboard] ...}
 {:path "/:page.html" :rules {:page #".+"} ...}]
```

Notice that the routes have carried the context so that the path contains the prefix. The route helpers will concatenate the :path, :rules and any :pre functions of the context. Subsequent context calls will continue this process so that context is always concatenated or passed through. 

All other context keys are passed through, but the route defs override anything else when a value is present.

#### Names and path generation
Routes can contain a :name key. The route example above contains a route with a name, :root. This name can be used later for finding a route, and building a path according to its path signature. Wire provides a helper for building paths:

```clojure
(path-for app-routes :admin-stats {:view "overview"})
;; "/admin/stats/overview"
```

### Middleware

In order to make use of the route definitions and dispatch within a Ring application, the routes will first need to be compiled:

```clojure
(def compiled-app-routes (compile-routes app-routes))
```

#### Dispatching

The dispatching in Wire is done in two steps.

##### Route identification

First, a suitable route definition is identified by asking the following questions:

  * do all of the pre-conditions return true?
  * does the request method match?
  * does the route path match?

If a route is matched, it's injected into the request map.

##### Handler execution

At the end of the stack is yet another piece of middleware for handling the execution of this route called exec-matched-route.

Here's the basic skeleton:

```clojure
(def my-ring-app
  (-> middle/exec-matched-route
      (middle/wrap-match compiled-app-routes)))
```

## License

Copyright © 2013 Matt Mitchell

Distributed under the Eclipse Public License, the same as Clojure.