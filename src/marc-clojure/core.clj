(ns marc-clojure.core
  (:require [clojure.string :as s]))

(defn iterator->lazyseq [iteration]
  (lazy-seq
   (reify java.lang.Iterable
     (iterator [this]
       (reify java.util.Iterator
         (hasNext [this] (.hasNext iteration))
         (next [this] (.next iteration))
         (remove [this] (.remove iteration)))))))

(defn read-file [filename]
  (-> filename java.io.FileInputStream. org.marc4j.MarcStreamReader. iterator->lazyseq))

(defmulti load-data identity)

(defmethod load-data :volter [_]
  (->> ["./volter1.mrc" "./volter2.mrc"]
       (map read-file)
       flatten))
 
(defmethod load-data :selma [_]
  (read-file "./parl.mrc"))

(defmethod load-data :alli [_]
  (read-file "./alli.mrc"))

(defmethod load-data :lamk [_]
  (read-file "./lamk.mrc"))

(defmethod load-data :tayk [_]
  (read-file "./tayk.mrc"))

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

(defn get-id [record]
  (->> (.getVariableField record "001")
       .getData
       s/trim))

(defn serialize-subfield [subfield]
  {:code (str (.getCode subfield))
   :data (.getData subfield)})

(defn datafield? [field]
  (if (> (Integer. (.getTag field)) 9) true false))

(defn serialize-field [field]
  (if-not (datafield? field)
    {:type "controlfield"
     :tag (.getTag field)
     :data (.getData field)}
    {:type "datafield"
     :tag (.getTag field)
     :subfields (->> (.getSubfields field)
                     seq
                     (map serialize-subfield)
                     vec)}))

(defn serialize-record [record]
  (letfn [(serialize-fields [fields]
            (->> fields seq (map serialize-field) vec))]
    {:bibid (get-id record)
     :leader (.toString (.getLeader record))
     :fields (vec (concat
                   (serialize-fields (.getControlFields record))
                   (serialize-fields (.getDataFields record))))}))

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

;;;;;;;;;;;;;;;;;;;;;;;
;; Record validators ;;
;;;;;;;;;;;;;;;;;;;;;;;

(defn record-contains? [record phrase]
  (-> (.toString record)
      s/lower-case
      (.contains phrase)))

(defn get-record-tags [record]
  (map #(.getTag %) (.getDataFields record)))

(defn contains-field? [record tag]
  (contains? (set (get-record-tags record)) tag))

;; Functions for tracking all known cases

;; 015 sidosasu ei ole siirtynyt $a-osakentästä $q-osakenttään

(defn sidosasu-015a? [record]
  (let [contents
        (->> (get-subfields record "015" "a")
             (map :data)
             (s/join ""))]
    (boolean (re-find #"sid|nid|pbk|hbk|inb|hft" contents))))

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

(defn wrong-indicator-245? [record]
  (when (contains-field? record "245")
    (let [i1 (-> record (.getVariableField "245")
                 .getIndicator1
                 (Character/digit 10))]
      (and
       (contains? (set (get-record-tags record)) "130")
       (= i1 0)))))

(defn has-lcsh-headings? [record]
  (when (contains-field? record "650")
    (->> (.getVariableFields record "650")
         (map #(.getIndicator2 %))
         (map #(Character/digit % 10))
         set
         (#(contains? % 0)))))

;;;

(defn erroneous?
  "Validate a record. This function takes a vector of validators
  and a record object and checks, if any of them returns true."
  [validators record]
  (->> (map #(% record) validators)
       (some true?)
       boolean))

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

