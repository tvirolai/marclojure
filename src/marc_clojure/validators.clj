(ns marc-clojure.validators
  (:require [marc-clojure.core :refer :all]
            [clojure.string :as s]
            [clj-isbn.core :as isbn]))

;; 015 sidosasu ei ole siirtynyt $a-osakentästä $q-osakenttään

(defn sidosasu-015a? [record]
  (let [contents (->> (get-subfields record "015" "a")
                      flatten
                      (map :data)
                      (s/join " "))]
    (boolean (re-find #"nid|sid|inb|pbk|hbk|hft|inb" contents))))

(defn text-in020z? 
  "Check if record contains subfield 020$z and if it
  contains text."
  [record]
  (let [field (->> (get-subfields record "020" "z")
                   flatten
                   (map :data)
                   (s/join " "))]
    (boolean (re-find #"[A-ZÅÄÖa-zöäå]{2,}" field))))

(defn invalid-020a? 
  "Check if record has a subfield 020$a and if it contains
  something else than just an ISBN code."
  [record]
  (let [fields (get-subfields record "020" "a")]
    (if-not (empty? fields) 
      (map :data fields)
      false)))

(defn empty-020a? 
  "Check if record contains an empty subfield 020$a."
  [record]
  (let [sf (get-subfields record "020" "a")
        content (->> sf flatten (map :data) (s/join " "))]
    (if (empty? sf) false
        (= "" content))))

(defn contains-024c? [record]
  (->> (get-subfields record "024" "c") count (< 0)))

(defn wrong-indicator-245? [record]
  (let [i1 (->> (get-fields record "245") first :i1)]
    (and
     (= "0" i1)
     (contains-field? record "130"))))

(defn regex-error-300? [record]
  (let [f300 (->> (get-fields record "300")
                  flatten
                  (map :data)
                  (s/join " "))]
    (boolean
     (re-find #"sid\.|nid.\|kas\.|kuultokuv\.|kuultokuvitettu|muotokuvitettu" f300))))

(defn get-functionterms [record]
  (let [getter (partial get-subfields record)
        f100e (getter "100" "e")
        f110e (getter "110" "e")
        f700e (getter "700" "e")
        f710e (getter "710" "e")
        results (flatten [f100e f110e f700e f710e])]
    (map :data results)))

(defn arkki-and-nide [record]
  (let [contents (->> (get-subfields record "338" "a") (map :data) set)]
    (every? #(contains? contents %) ["arkki"])))

(defn verkkojulkaisu? [record]
  (record-contains-phrase record ["verkkojulkaisu"]))

(defn internet-julkaisu [record]
  (record-contains-phrase record ["internet-julkais"]))

(defn extra-338-nide? [record]
  (boolean
    (and
      (field-contains-phrase record "300" ["erkkoaineisto"])
      (field-contains-phrase record "338" ["nide"])
      (field-contains-phrase record "338" ["verkkoaineisto"]))))

(defn get-id-if-extra338 [record]
  (if (extra-338-nide? record) (:bibid record) ""))

(defn contains-erroneous-functionterms? [record]
  (let [errfunc ["(ed.)" "säv." "kirj." "(orig.)" "(trans.)" 
                 "cover suunnittelija." "respondenttiti" 
                 "kirjoittaja of introduction, etc." 
                 "julk." "ed." "kirjap." "resp." "(introd.)" 
                 "grat." "aut" "toimittaja,." "kirjoittaja,." 
                 "esitt." "sov." "toim." "kirjoittaja of afterword, colophon, etc." 
                 "comp." "kommentaattori for written text." 
                 "joint kirjoittaja." "publishing ohjaaja." 
                 "kirjoittaja of foreword." "contributing toimittaja."]
        functions (get-functionterms record)]
   (->> (clojure.set/intersection (set errfunc) (set functions))
        count
        pos?)))

(defn errors-in-300? [record]
  (field-contains-phrase record "300" ["nid." "sid." "kuultokuv."
                                       "kas." "taulukkol."
                                       "kuv." "kartt." "karttal."]))

(defn wrong-338-arkki? [record]
  (boolean
    (and
      (field-contains-phrase record "338" ["arkki"])
      (field-contains-phrase record "338" ["nide"])
      (record-contains-phrase record ["tarra-arkki" "kartasto"]))))

(defn function-term-report [dataset]
  (loop [data dataset
         report {}]
    (if (empty? data)
      report
      (recur (rest data)
             (merge-with +
                         report
                         (->> dataset first get-functionterms frequencies))))))

(def alli-validators [sidosasu-015a?
                      text-in020z?
                      contains-024c?
                      wrong-indicator-245?
                      regex-error-300?])

(defn erroneous?
  "Validate a record. This function takes a vector of validators
  and a record object and checks, if any of them returns true."
  [validators record]
  (->> (map #(% record) validators)
       (some true?)
       boolean))
