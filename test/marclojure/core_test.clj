(ns marclojure.core-test
  (:require [clojure.test :refer :all]
            [marclojure.core :refer :all]
            [marclojure.parser :refer :all]
            [marclojure.writer :refer :all]))

(def testrecord
  (read-string (slurp "./testdata/testrecord.edn")))

(def testrec-marc
  (load-data :marc "./testdata/testdata.mrc"))

(def testrec-xml
  (load-data :marcxml "./testdata/testdata.xml"))

(def testrec-aleph
  (load-data :aleph "./testdata/testdata.seq"))

(deftest accessors
  (testing "Membership in record"
    (is (true? (contains-field? "338" testrecord)))
    (is (false? (contains-field? "339" testrecord)))))

(deftest fieldoperations
  (testing "Field checks"
    (is (true? (datafield? (first (get-fields "245" testrecord)))))
    (is (false? (datafield? (first (get-fields "008" testrecord)))))))

(deftest phrases
  (testing "Record should contain phrase 'intervention'"
    (is (true? (record-contains-phrase? ["intervention"] testrecord)))
    (is (true? (record-contains-phrase? ["intervention" "necro"] testrecord)))
    (is (true? (record-contains-phrase? ["Division of Science"] testrecord)))
    (is (false? (record-contains-phrase? ["we the best"] testrecord))))
  (testing "Capitalization should not matter"
    (is (true? (record-contains-phrase? ["division of science"] testrecord)))
    (is (true? (record-contains-phrase? ["DIVISION OF SCIENCE"] testrecord))))
  (testing "should return true if any of the input strings are found"
    (is (true? (field-contains-phrase? "338" ["nide"] testrecord)))
    (is (true?
          (field-contains-phrase? "338"
                                  ["nide" "Bama lama" "HEAVY METAL"]
                                  testrecord)))
    (is (false? (field-contains-phrase? "338" ["yeehaw" "hello"] testrecord)))))

(deftest parsing
  (testing "ISO 2709 parsing succeeds"
    (is (true? (seq? testrec-marc))))
  (testing "Aleph Sequential parsing succeeds"
    (is (true? (seq? testrec-aleph))))
  (testing "MARCXML parsing succeeds"
    (is (true? (seq? testrec-xml))))
  (testing "Record structure should be sane, with a 245 field in each"
    (is (true? (every? true? (map #(contains-field? "245" %) testrec-marc))))
    (is (true? (every? true? (map #(contains-field? "245" %) testrec-aleph))))
    (is (true? (every? true? (map #(contains-field? "245" %) testrec-xml))))))
