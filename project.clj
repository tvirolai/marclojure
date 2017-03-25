(defproject marclojure "0.2.6"
  :description "A library for parsing MARC records"
  :url "http://github.com/tvirolai/marclojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.marc4j/marc4j "2.8.0"]
                 [org.clojure/data.xml "0.0.8"]]
  :plugins [[lein-cloverage "1.0.9"]])
