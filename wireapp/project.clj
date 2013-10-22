(defproject wireapp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.codesignals/wire "0.4.0"]
                 [org.clojure/tools.logging "0.2.6"]
                 [hiccup "1.0.4"]
                 [ring/ring-core "1.1.7"]]
  :ring {:handler wireapp.core/handler})
