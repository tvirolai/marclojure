(ns user
  (:require [marclojure.core :as marc]
            [marclojure.parser :as parser]
            [marclojure.writer :as writer]))

;; A testbed namespace

(def file
  "/home/tvirolai/Desktop/e-aineistoselvitys/ematerials.seq")

(def testset
  (take 10 (parser/load-data :aleph file)))

(def testbatch
  (parser/load-data :aleph "./testdata/testbatch.seq"))
