(ns user
  (:require [marclojure.core :as marc]
            [marclojure.parser :as parser]
            [marclojure.writer :as writer]))

;; A testbed namespace

(def file
  "/home/tvirolai/Melinda-dumppi/dumppi.seq")

(defn calculate []
  (->> file (parser/load-data :aleph) count))

(def tietue
  (->> file (parser/load-data :aleph) second))
