(defproject marclojure "1.0.6-SNAPSHOT"
  :description "A library for parsing MARC records"
  :url "http://github.com/tvirolai/marclojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "LATEST"]
                 [org.marc4j/marc4j "2.9.2"]
                 [org.clojure/data.xml "0.0.8"]]
  :plugins [[lein-cloverage "1.0.11-SNAPSHOT"]]
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :creds :gpg
                                    :sign-releases false}]]
  :profiles {:dev {:source-paths ["dev"]}})
