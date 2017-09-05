(ns marclojure.writer
  (:require [clojure.string :as s]
            [clojure.data.xml :as xml]
            [marclojure.parser :refer [load-data]]
            [marclojure.core :as core]))

(defn- field-length
  "Get field length of the binary representation."
  [field]
  (if (= "controlfield" (:type field))
    (inc (count (:data field)))
    (let [sfs (:subfields field)]
      (reduce + 3
        (map #(+ 2 (count (:data %))) sfs)))))

(defn- number-padding
  "Parses a number to a string with padding of zeroes.
  Example: 4 -> 0004"
  [number length]
  (format (str "%0" length "d") number))

(defn parse-directory [record]
  (loop [dir ""
         index 0
         curr (first (:fields record))
         fields (rest (:fields record))]
    (if (empty? fields)
      dir
      (recur
        (str dir (:tag curr)
             (number-padding (field-length curr) 4)
             (number-padding index 5))
        (+ index (field-length curr))
        (first fields)
        (rest fields)))))

(defn- rec-fmt
  "Define a record format for Aleph Sequential FMT field."
  [record]
  (let [leader (:leader record)
        l6 (subs leader 6 7)
        l7 (subs leader 7 8)]
    (cond
      (and (contains? #{"a" "t"} l6)
           (not (contains? #{"b" "i" "s"} l7))) "BK"
      (= "m" l6) "CF"
      (and (contains? #{"a" "t"} l6)
           (contains? #{"b" "i" "s"} l7)) "CR"
      (contains? #{"e" "f"} l6) "MP"
      (contains? #{"c" "d" "i" "j"} l6) "MU"
      (= "p" l6) "MX"
      (contains? #{"g" "k" "o" "r"} l6) "VM"
      :default "UNRECOGNIZED")))

(defn- parse-field-aleph [id field]
  (let [i1 (if (core/datafield? field) (:i1 field) " ")
        i2 (if (core/datafield? field) (:i2 field) " ")
        content (s/replace
                  (subs (core/field-to-string field) 7)
                  #"\$"
                  "\\$\\$")]
    (str id " " (:tag field) i1 i2 " L " content)))

(defn to-aleph [record]
  (let [parsed-id (number-padding (Integer. (:bibid record)) 9)
        line-fmt (str parsed-id " FMT   L " (rec-fmt record))
        line-ldr (str parsed-id " LDR   L " (:leader record))
        lines (map (partial parse-field-aleph parsed-id) (:fields record))]
    (str line-fmt "\n" line-ldr "\n" (s/join "\n" lines) "\n")))

(defn- subfield-to-xml [sf]
  (xml/element :subfield {:code (:code sf)} (:data sf)))

(defn- field-to-xml [field]
  (if (= "controlfield" (:type field))
    (xml/element :controlfield {:tag (:tag field)} (:data field))
    (xml/element :datafield {:tag (:tag field)
                             :ind1 (:i1 field)
                             :ind2 (:i2 field)}
                 (map subfield-to-xml (:subfields field)))))

(defn to-marcxml [record]
  (xml/element :record {}
               (xml/element :leader {} (:leader record))
               (map field-to-xml (:fields record))))

(defn to-xml-string [record]
  (->> record to-marcxml xml/emit-str))

(defmulti write-data (fn [format data filename] format))

(defmethod write-data :marcxml [_ data filename]
  (let [xmldata (if (seq? data)
                  (map to-marcxml data)
                  (to-marcxml data))
        outputdata (xml/element
                     :collection
                     {:xmlns "http://www.loc.gov/MARC21/slim"}
                     xmldata)]
    (with-open [out (java.io.FileWriter. filename)]
      (xml/emit outputdata out))))

(defmethod write-data :aleph [_ data filename]
  (with-open [out (clojure.java.io/writer filename)]
    (binding [*out* out]
      (if (seq? data)
        (println (s/join (map to-aleph data)))
        (println (str (to-aleph data)))))))
