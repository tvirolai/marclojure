(ns marc-clojure.core
  (:require [clojure.string :as s]
            [marc-clojure.parse :refer [load-data]]))

(defn get-fields [record tag]
  (->> record :fields (filter #(= tag (:tag %)))))

(defn contains-field? [record tag]
  (< 0 (count (get-fields record tag))))

(defn get-subfields
  ([field code]
   (->> field :subfields (filter #(= code (:code %)))))
  ([record tag code]
   (->> (get-fields record tag) (map #(get-subfields % code)) flatten)))

(defn datafield? [field]
  (= "datafield" (:type field)))

(defn field-to-string [field]
  (if (datafield? field)
    (apply str (:tag field) " "
         (:i1 field)
         (:i2 field) " "
         (map (fn [sf] (str "$" (:code sf) (:data sf))) (:subfields field)))
    (apply str (:tag field) "    " (:data field))))

(defn to-string [record]
  (let [leader (str "000    " (:leader record))
        fields (:fields record)]
    (apply str leader "\n" (s/join "\n" (map field-to-string fields)))))

(defn print-to-file [recs filename]
  (when (not (empty? recs))
    (do
      (spit filename (str (to-string (first recs)) "\n\n") :append true)
      (recur (rest recs) filename))))

(defn print-ids-to-file [recs filename]
  (when (not (empty? recs))
    (->> recs
         (map :bibid)
         (s/join "\n")
         (apply str)
         (spit filename))))

(defn record-contains-phrase 
  "Takes a record and a vector of phrases.
  Returns true if the record contains some of the phrases."
  [record phrases]
  (let [recstring (s/lower-case (to-string record))]
    (boolean (some true? (map #(.contains recstring (s/lower-case %)) phrases)))))

(defn field-contains-phrase [record tag phrases]
  (let [field (->> (get-fields record tag) (map field-to-string) (s/join " ") s/lower-case)]
    (boolean (some true? (map #(.contains field (s/lower-case %)) phrases)))))

(defn field-report [batch tag]
  (->> batch (map #(get-fields % tag)) (filter not-empty) flatten (map field-to-string)))
