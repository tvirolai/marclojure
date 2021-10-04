(defproject marclojure "1.0.6-SNAPSHOT"
  :description "A library for parsing MARC records"
  :url "http://github.com/tvirolai/marclojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "LATEST"]
                 [org.marc4j/marc4j "2.9.2"]
                 [org.clojure/data.xml "0.0.8"]]
  :plugins [[lein-shell "0.5.0"]]
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :creds :gpg
                                    :sign-releases false}]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["shell" "git" "commit" "-am" "Version ${:version} [ci skip]"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["shell" "git" "commit" "-am" "Version ${:version} [ci skip]"]
                  ["vcs" "push"]]
  :profiles {:dev {:source-paths ["dev"]}})
