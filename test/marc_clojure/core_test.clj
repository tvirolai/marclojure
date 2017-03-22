(ns marc-clojure.core-test
  (:require [clojure.test :refer :all]
            [marc-clojure.core :refer :all]
            [marc-clojure.parse :refer :all]))

(def testrecord
  (read-string (slurp "./data/testrecord.edn")))

(deftest accessors
  (testing "Membership in record"
    (is (true? (contains-field? testrecord "338")))
    (is (false? (contains-field? testrecord "339")))))

(deftest fieldoperations
  (testing "Field checks"
    (is (true? (datafield? (first (get-fields testrecord "245")))))
    (is (false? (datafield? (first (get-fields testrecord "008")))))))

(deftest phrases
  (testing "Record should contain phrase 'intervention'"
    (is (true? (record-contains-phrase testrecord ["intervention"])))
    (is (true? (record-contains-phrase testrecord ["intervention" "necro"])))
    (is (true? (record-contains-phrase testrecord ["Division of Science"])))
    (is (false? (record-contains-phrase testrecord ["we the best"]))))
  (testing "Capitalization should not matter"
    (is (true? (record-contains-phrase testrecord ["division of science"])))
    (is (true? (record-contains-phrase testrecord ["DIVISION OF SCIENCE"]))))
  (testing "field-contains-phrase should return true if any of the input strings are found"
    (is (true? (field-contains-phrase testrecord "338" ["nide"])))
    (is (true? (field-contains-phrase testrecord "338" ["nide" "Bama lama" "HEAVY METAL"])))
    (is (false? (field-contains-phrase testrecord "338" ["yeehaw" "hello"])))))

