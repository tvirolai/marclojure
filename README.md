# marclojure

[![Clojars Project](https://img.shields.io/clojars/v/marclojure.svg)](https://clojars.org/marclojure)
[![Build Status](https://travis-ci.org/tvirolai/marclojure.svg?branch=master)](https://travis-ci.org/tvirolai/marclojure)
[![codecov](https://codecov.io/gh/tvirolai/marclojure/branch/master/graph/badge.svg)](https://codecov.io/gh/tvirolai/marclojure)
[![Downloads](https://jarkeeper.com/tvirolai/marclojure/downloads.svg)](https://jarkeeper.com/tvirolai/marclojure)
[![Dependencies Status](https://jarkeeper.com/tvirolai/marclojure/status.png)](https://jarkeeper.com/tvirolai/marclojure)

## About

*marclojure* is a library for - can you guess? - processing [MARC records](https://en.wikipedia.org/wiki/MARC_standards) using Clojure. It can be used to serialize MARC records in ISO 2709 (MARC exchange format), MARCXML or Aleph Sequential formats into Clojure maps, process them and write them back to file. Writing is currently possible in MARCXML and Aleph Sequential, ISO 2709 is going to be supported very soon.

## Latest version

[![Clojars Project](http://clojars.org/marclojure/latest-version.svg)](http://clojars.org/marclojure)

## Installation

marclojure is available from [Clojars](https://clojars.org/marclojure). Add it to your `project.clj` as follows:

```clojure
[marclojure "1.0.6-SNAPHOT"]
```

Then you can require it into your namespace:

```clojure
(ns foo.bar
  (:require [marclojure.core :as marc]
            [marclojure.parser :as parser]
            [marclojure.writer :as writer]))
```

## Usage

MARC batch files can be read into lazy sequences using the `load-data` multimethod from `marclojure.parser` namespace.
Load-data accepts two arguments: file format (keyword, possible options are `:marc`, `:marcxml` or `:aleph`) and a filename.

In older versions of marclojure, system-specific fields (LOW, SID, FMT etc.) were not retained when parsing Aleph Sequential data. From 1.0.4 they are retained and can be optionally weeded by calling marclojure.core/remove-aleph-fields on the record.

An example:

```clojure
(def dataset (parser/load-data :marc "somefile.mrc"))
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

Apart from parsing MARC data, the `marclojure.core` namespace provides some utility functions
for processing record sequences. Some examples (here the core namespace is loaded as `marc`, see above).

```clojure
(def batch (parser/load-data :marc "marcdata.mrc"))
=> #'foo.bar/batch
```

```clojure
(def record (first batch))
=> #'foo.bar/record
```

```clojure
(marc/print-to-repl record))
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
(marc/get-subfields "245" "a" record)
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
(marc/record-contains-phrase? ["lausanne" "hard rock"] record)
=> true
```

```clojure
(marc/contains-field? "130" record)
=> false
```

```clojure
(marc/field-contains-phrase? "100" ["Simon"] record)
=> true

```

Writing records to file is done as follows:

```clojure
(writer/write-data :marcxml batch "outputfile.xml")
=> nil
(writer/write-data :aleph batch "outputfile.seq")
=> nil
```

## Thanks

`marclojure` uses [marc4j](https://github.com/marc4j/marc4j) for reading MARC data. Thanks for that!

Aleph Sequential parser is based on [clj-marc](https://github.com/phochste/clj-marc).

## License

Copyright © 2017-2021 Tuomo Virolainen

Distributed under the Eclipse Public License either version 1.0.
