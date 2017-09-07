(ns marclojure.core
  (:require [clojure.string :as s]
            [marclojure.parser :refer [load-data]]))

(defn get-fields [tag record]
  (->> record :fields (filter #(= tag (:tag %)))))

(defn contains-field? [tag record]
  (pos? (count (get-fields tag record))))

(defn get-subfields
  ([field code]
   (->> field :subfields (filter #(= code (:code %)))))
  ([tag code record]
   (->> (get-fields tag record) (map #(get-subfields % code)) flatten)))

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
  (when (seq recs)
    (spit filename (str (to-string (first recs)) "\n\n") :append true)
    (recur (rest recs) filename)))

(defn print-to-repl
  "Show a single record in human-readable format in the REPL"
  [record]
  (doseq [line (s/split (to-string record) #"\n")]
    (println line)))

(defn print-ids-to-file
  "Takes a batch of records and a filename,
  prints the ID's of the recs in the file."
  [recs filename]
  (when (seq recs)
    (->> recs
         (map :bibid)
         (s/join "\n")
         (apply str)
         (spit filename))))

(defn record-contains-phrase?
  "Takes a record and a vector of phrases.
  Returns true if the record contains some of the phrases."
  [phrases record]
  (let [recstring (s/lower-case (to-string record))]
    (boolean
      (some true?
            (map #(.contains recstring (s/lower-case %)) phrases)))))

(defn field-contains-phrase? [tag phrases record]
  (let [field (->> (get-fields tag record)
                   (map field-to-string)
                   (s/join " ")
                   s/lower-case)]
    (boolean
      (some true?
            (map #(.contains field (s/lower-case %)) phrases)))))

(defn field-report [tag batch]
  (->> batch
       (map (partial get-fields tag))
       (filter not-empty)
       flatten
       (map field-to-string)))

(defn is-aleph-field? [field]
  (->> field :tag (re-seq #"[A-Z]") ((complement nil?))))

(defn remove-aleph-fields
  "Weeds all fields with alphabetical tags (LOW, STA, SID) from record."
  [record]
  {:leader (:leader record)
   :bibid (:bibid record)
   :fields (filterv (complement is-aleph-field?) (:fields record))})
