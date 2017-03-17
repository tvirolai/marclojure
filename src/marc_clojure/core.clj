(ns marc-clojure.core
  (:require [clojure.string :as s]))

(defn iterator->lazyseq
  "Input Java iterable, return a lazy sequence"
  [iteration]
  (lazy-seq
   (reify java.lang.Iterable
     (iterator [this]
       (reify java.util.Iterator
         (hasNext [this] (.hasNext iteration))
         (next [this] (.next iteration))
         (remove [this] (.remove iteration)))))))

(defn read-file [filename]
  (-> filename java.io.FileInputStream. org.marc4j.MarcStreamReader. iterator->lazyseq))

(defn- datafield? [field]
  (if (> (Integer. (.getTag field)) 9) true false))

(defn- datafield?* [tagstring]
  (let [tag-number (try (Integer. tagstring)
                        (catch Exception e
                          (println (str "Non-numeral field: " tagstring))
                          999))]
    (if (> tag-number 9) true false)))

(defn- get-id [record]
  (->> (.getVariableField record "001")
       .getData
       s/trim))

(defn serialize-subfield [subfield]
  {:code (str (.getCode subfield))
   :data (.getData subfield)})

(defn serialize-field [field]
  (if-not (datafield? field)
    {:type "controlfield"
     :tag (.getTag field)
     :data (.getData field)}
    {:type "datafield"
     :i1 (str (.getIndicator1 field))
     :i2 (str (.getIndicator2 field))
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

(defn- parse-subfields [fieldcontent]
  (->> (s/split fieldcontent #"\$")
       (filter not-empty)
       (map #(hash-map :code (subs % 0 1)
                       :data (subs % 1)))
       vec))

(defn serialize-field* [fieldstring]
  (let [tag (subs fieldstring 0 3)]
    (if-not (datafield?* tag)
      {:type "controlfield"
       :tag tag
       :data (subs fieldstring 4)}
      {:type "datafield"
       :tag tag
       :i1 (subs fieldstring 4 5)
       :i2 (subs fieldstring 5 6)
       :subfields (parse-subfields (subs fieldstring 6))})))

(defn serialize-record* [recstring]
  (let [elements (s/split recstring #"\n")
        leader (first elements)
        fields (->> elements rest (map serialize-field*) vec)]
    {:bibid (->> fields (filter #(= "001" (:tag %))) first :data s/trim)
     :leader (subs leader 7)
     :fields fields}))

(defmulti load-data identity)

(defmethod load-data :volter [_]
  (->> ["./data/volter1.mrc" "./data/volter2.mrc"]
       (map read-file)
       flatten
       (pmap (comp serialize-record* #(.toString %)))))

(defmethod load-data :selma [_]
  (->> "./data/parl.mrc"
       read-file
       (pmap (comp serialize-record* #(.toString %)))))

(defmethod load-data :fennica [_]
  (->> "./data/fennica.mrc"
       read-file
       (pmap (comp serialize-record* #(.toString %)))))

(defmethod load-data :alli [_]
  (->> "./data/alli.mrc"
       read-file
       (pmap (comp serialize-record* #(.toString %)))))

(defmethod load-data :lamk [_]
  ;(read-file "./data/lamk.mrc")
  (->> "./data/lamk.mrc"
       read-file
       (pmap (comp serialize-record* #(.toString %)))))

(defmethod load-data :tayk [_]
  (read-file "./data/tayk.mrc"))

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

