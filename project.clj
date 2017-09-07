(defproject marclojure "1.0.4"
  :description "A library for parsing MARC records"
  :url "http://github.com/tvirolai/marclojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.marc4j/marc4j "2.8.2"]
                 [org.clojure/data.xml "0.0.8"]]
  :profiles {:dev {:source-paths ["dev"]}})
