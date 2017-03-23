# marc-clojure

[![Clojars Project](https://img.shields.io/clojars/v/marc-clojure.svg)](https://clojars.org/marc-clojure)
[![Build Status](https://travis-ci.org/tvirolai/marc-clojure.svg?branch=master)](https://travis-ci.org/tvirolai/marc-clojure)
[![Downloads](https://jarkeeper.com/tvirolai/marc-clojure/downloads.svg)](https://jarkeeper.com/tvirolai/marc-clojure)
[![Dependencies Status](https://jarkeeper.com/tvirolai/marc-clojure/status.png)](https://jarkeeper.com/tvirolai/marc-clojure)

## About

*marc-clojure* is a library for - can you guess? - processing [MARC records](https://en.wikipedia.org/wiki/MARC_standards) using Clojure. It can be used to
serialize MARC records in ISO 2709 (MARC exchange format), MARCXML or Aleph Sequential formats into Clojure maps. Writing
records back to file is not yet supported, but this functionality is upcoming.

## Latest version

[![Clojars Project](http://clojars.org/marc-clojure/latest-version.svg)](http://clojars.org/marc-clojure)

## Installation

marc-clojure is available from [Clojars](https://clojars.org/marc-clojure). Add it to your `project.clj` as follows:

```clojure
[marc-clojure "0.2.0"]
```

Then you can require it into your namespace:

```clojure
(ns foo.bar
  (:require [marc-clojure.core :as marc]
            [marc-clojure.parse :as parse]))
```

## Usage

MARC batch files can be read into lazy sequences using the `load-data` multimethod from `clojure-marc.parse` namespace.
Load-data accepts two arguments: file format (keyword, possible options are `:marc`, `:marcxml` or `:aleph`) and a filename.

An example:

```clojure
(def dataset (parse/load-data :marc "somefile.mrc"))
=> #'foo.bar/dataset
```

Serialized records are represented as Clojure maps. The format looks as follows:

```clojure
{:bibid "2"
 :leader "01066cam a22003137i 4500"
 :fields [{:type "controlfield", :tag "001", :data "  2"}
          {:type "controlfield", :tag "005", :data "20120402125847.0"}
          {:type "controlfield", :tag "008", :data "881209s1986    fr ||||||b   |||||||eng||"}
          {:type "datafield"
           :tag "020"
           :i1 " "
           :i2 " "
           :subfields [{:code "a", :data "9780306406157 (hbk.)"}]}
          {:type "datafield", :tag "245"
           :i1 "0"
           :i2 "0"
           :subfields [{:code "a", :data "Health education intervention :"}
                       {:code "b", :data "an annotated bibliography /"}
                       {:code "c", :data "Unesco Nutrition Education Programme ; Division of Science, Technical and Environmental Education, Unesco."}]}
          {:type "datafield"
           :tag "260"
           :i1 " "
           :i2 " "
           :subfields [{:code "a", :data "Paris :"}
                       {:code "b", :data "Unesco,"}
                       {:code "c", :data "1986."}]}
          {:type "datafield"
           :tag "300"
           :i1 " "
           :i2 " "
           :subfields [{:code "a", :data "103 sivua"}]}
          {:type "datafield"
           :tag "336"
           :i1 " "
           :i2 " "
           :subfields [{:code "a", :data "teksti"}
                       {:code "b", :data "txt"}
                       {:code "2", :data "rdacontent"}]}
          {:type "datafield"
           :tag "337"
           :i1 " "
           :i2 " "
           :subfields [{:code "a", :data "käytettävissä ilman laitetta"}
                       {:code "b", :data "n"}
                       {:code "2", :data "rdamedia"}]}
          {:type "datafield"
           :tag "338"
           :i1 " "
           :i2 " "
           :subfields [{:code "a", :data "nide"}
                       {:code "b", :data "nc"}
                       {:code "2", :data "rdacarrier"}]}
          {:type "datafield"
           :tag "490"
           :i1 "1"
           :i2 " "
           :subfields [{:code "a", :data "Nutrition education series ;"}
                       {:code "v", :data "13"}]}
          {:type "datafield"
           :tag "515"
           :i1 " "
           :i2 " "
           :subfields [{:code "a", :data "Unesco doc. ED-86/WS/83."}]}
          {:type "datafield"
           :tag "650"
           :i1 " "
           :i2 "7"
           :subfields [{:code "a", :data "terveydenhuolto"}
                       {:code "x", :data "bibliografia"}
                       {:code "2", :data "eks"}]}
          {:type "datafield"
           :tag "830"
           :i1 " "
           :i2 "0"
           :subfields [{:code "a", :data "Nutrition education series ;"}
                       {:code "v", :data "13."}]}
          {:type "datafield"
           :tag "852"
           :i1 " "
           :i2 " "
           :subfields [{:code "a", :data "FI-E"}
                       {:code "b", :data "IV.3."}
                       {:code "c", :data "Unesco 2-464"}]}]}
```

Apart from parsing MARC data, the `marc-clojure.core` namespace provides some utility functions
for processing record sequences. Some examples (here the core namespace is loaded as `marc`, see above).

```clojure
(def batch (parse/load-data :marc "marcdata.mrc"))
=> #'foo.bar/batch
```

```clojure
(def record (first batch))
=> #'foo.bar/record
```

```clojure
(marc/to-string record))
=>
"000    00000cam^a22004097i^4500
 001    000000002
 005    20160406135147.0
 008    850308s1980^^^^sz^|||||||||||||||||fre||
 041 0  $afre
 080    $a696/697
 080    $a296.63
 080    $a929 Josephus
 100 0  $aSzyszman, Simon.
 245 13 $aLe karaïsme :$bses doctrines et son histoire /$cSimon Szyszman.
 260    $aLausanne :$bL'Age d'Homme,$c1980.
 300    $a247 s., 24 pl. :$bill., kart.
 336    $ateksti$btxt$2rdacontent
 337    $akäytettävissä ilman laitetta$bn$2rdamedia
 338    $anide$bnc$2rdacarrier
 490 1  $aBibliotheca karaitica. Series A ;$vvol. 1
 650  7 $atalotekniikka$2ysa"
```

```clojure
(-> record (marc/get-fields "245") first field-to-string)
=> "245 13 $aLe karaïsme :$bses doctrines et son histoire /$cSimon Szyszman."
```

```clojure
(marc/get-subfields record "245" "a")
=> ({:code "a", :data "La karaisme"})
```

```clojure
(marc/print-to-file batch "outputfile.txt")
=> nil
```

```clojure
(marc/print-ids-to-file batch "outputfile_ids.txt")
=> nil
```

```clojure
(marc/record-contains-phrase? record ["lausanne" "hard rock"])
=> true
```

```clojure
(marc/contains-field? record "130")
=> false
```

```clojure
(marc/field-contains-phrase? record "100" "Simon")
=> true

```

## TODO

* Writing serialized records to file
* Remove `freelib-marc4j` Java library from dependencies, move to pure Clojure implementation
* Add test coverage

## Thanks

`clojure-marc` uses [freelib-marc4j](https://github.com/ksclarke/freelib-marc4j) for reading MARC data. Thanks for that!

Aleph Sequential parser is based on [clj-marc](https://github.com/phochste/clj-marc).

## License

Copyright © 2017 Tuomo Virolainen

Distributed under the Eclipse Public License either version 1.0.
