(ns user
  (:require [marclojure.core :as marc]
            [marclojure.parser :as parser]
            [marclojure.writer :as writer]))

(def test "test")

(def file
  "/home/tvirolai/Melinda-dumppi/dumppi.seq")

(defn calculate []
  (->> file (parser/load-data :aleph) count))
