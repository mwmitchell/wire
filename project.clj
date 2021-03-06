(defproject com.codesignals/wire "0.4.1"
  :description "A routing lib for Clojure and Ring"
  :url "http://github.com/mwmitchell/wire"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clout "1.1.0"]
                 [compojure "1.1.5"]
                 [org.clojure/tools.logging "0.2.6"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}})
