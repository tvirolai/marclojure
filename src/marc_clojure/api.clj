(ns marc-clojure.api
  (:require [clojure.string :as s]))

;; Here are the old functions that use the marc4j Java library

(defn print-rec [rec]
  (for [line (s/split (.toString rec) #"\n")]
    (println line)))

(defn get-titles [sequence]
  (map #(.toString (try (.getVariableField % "245") (catch Exception e (prn "Error: no title")))) sequence))

(defn get-title [record]
  (try (.toString (.getVariableField record "245"))
       (catch Exception e (println (str "Virhe: " e " " record)))
       (finally "")))

(defn title-contains? [phrase record]
  (let [title (get-title record)]
    (if (string? title) (.contains title phrase) false)))

(defn field-contains? [field phrase]
  (let [text (try (.toString field)
                  (catch Exception e (println e)))]
    (if (string? text) (.contains text phrase) false)))

(defn fields-that-contain [phrase record]
  (.find record phrase))

(defn get-fields [record tag]
  (.getVariableField record tag))

(defn record-contains? [record phrase]
  (-> (.toString record)
      s/lower-case
      (.contains phrase)))

(defn get-record-tags [record]
  (map #(.getTag %) (.getDataFields record)))

(defn contains-field? [record tag]
  (contains? (set (get-record-tags record)) tag))

(defn kuultokuvitettu? [record]
  (record-contains? record "kuultokuvitettu"))

(defn muotokuvitettu? [record]
  (record-contains? record "muotokuvitettu"))

(defn regex-virhe? [record]
  (or
   (muotokuvitettu? record)
   (kuultokuvitettu? record)))

(defn verkkojulkaisu? [record]
  (or
   (record-contains? record "erkkoversio")
   (record-contains? record "erkkojulkai")))

(defn wrong-indicator-245?* [record]
  (when (contains-field? record "245")
    (let [i1 (-> record (.getVariableField "245")
                 .getIndicator1
                 (Character/digit 10))]
      (and
       (contains? (set (get-record-tags record)) "130")
       (= i1 0)))))

(defn wrong-indicator-245? [record]
  (let [field (serialize-field (.getVariableField record "245"))]
    (and
     (contains-field? record "130")
     (= "0" (:i1 field)))))

(defn has-lcsh-headings? [record]
  (when (contains-field? record "650")
    (->> (.getVariableFields record "650")
         (map #(.getIndicator2 %))
         (map #(Character/digit % 10))
         set
         (#(contains? % 0)))))

(defn missing-300-but-contains-256? [record]
  (and (contains-field? record "256")
       (false? (contains-field? record "300"))))

(def xamk-validations
  [wrong-indicator-245?
   missing-300-but-contains-256?
   verkkojulkaisu?
   has-lcsh-headings?])

(defn get-datafield-content [record tag]
  (when (contains-field? record tag)
    (let [data (try (.getVariableField record tag)
                    (catch Exception e (prn e)))]
      (-> (.toString data) (subs 6)))))

(defn get-fields [record tag]
  (->> (serialize-record record)
       :fields
       (filter #(= tag (:tag %)))))

(defn get-subfields*
  ([field code]
   (->> field :subfields (filter #(= code (:code %)))))
  ([record tag code]
   (->> (get-fields record tag)
        (map :subfields)
        flatten
        (filter #(= code (:code %))))))

(defn get-subfields [record tag code]
  (let [field (.getVariableField record tag)]
    (if (nil? field) field
        (-> field serialize-field (get-subfields* code)))))
