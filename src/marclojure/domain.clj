(ns marclojure.domain
  (:require [clojure.spec.alpha :as s]))

;; A model for the different elements

(s/def ::leader string?)
(s/def ::bibid string?)

(s/def ::code (s/and string? #(= 1 (count %))))
(s/def ::data string?)
(s/def ::type #(= % (or "datafield" "controlfield")))

(s/def ::subfield
  (s/keys
    :req-un [::code ::data]))

(s/def ::indicator (s/and string? #(= 1 (count %))))

(s/def ::tag (s/and string? #(= 3 (count %))))

(s/def ::subfields (s/coll-of ::subfield))

(s/def ::i1 ::indicator)
(s/def ::i2 ::indicator)

(s/def ::controlfield
  (s/keys :req-un [::data ::type ::tag]))

(s/def ::datafield
  (s/keys :req-un [::i1 ::i2 ::subfields ::tag ::type]))
