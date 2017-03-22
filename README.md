# marc-clojure

[![Clojars Project](https://img.shields.io/clojars/v/marc-clojure.svg)](https://clojars.org/marc-clojure)
[![Build Status](https://travis-ci.org/tvirolai/marc-clojure.svg?branch=master)](https://travis-ci.org/tvirolai/marc-clojure)

A library for processing MARC records in Clojure.

## About

*marc-clojure* is a library for processing [MARC records](https://en.wikipedia.org/wiki/MARC_standards) using Clojure.


## Latest version

[![Clojars Project](http://clojars.org/marc-clojure/latest-version.svg)](http://clojars.org/marc-clojure)


## Installation

marc-clojure is available from Clojars. Add it to your `project.clj` as follows:

```clojure
[marc-clojure "0.1.0-SNAPSHOT"]
```

Then you can require it into your namespace:

```clojure
(ns foo.bar
  (:require [marc-clojure.core :as marc]
            [marc-clojure.parse :as parse]))
```

## TODO

* Writing serialized records to file
* Remove `freelib-marc4j` Java library from dependencies, move to pure Clojure implementation
* Add test coverage

## Usage

TODO

## License

Copyright © 2017 Tuomo Virolainen

Distributed under the Eclipse Public License either version 1.0.
