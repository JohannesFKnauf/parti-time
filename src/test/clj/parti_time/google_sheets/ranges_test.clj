(ns parti-time.google-sheets.ranges-test
  (:require [clojure.test :as t]
            [parti-time.google-sheets.ranges :as sut]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

(t/deftest column-index
  (t/testing "Converting column indexes in ABC notation to int"
    (t/is (= 1 (sut/AZ->int "A")))
    (t/is (= 2 (sut/AZ->int "B")))
    (t/is (= 26 (sut/AZ->int "Z")))
    (t/is (= 27 (sut/AZ->int "AA")))
    (t/is (= 28 (sut/AZ->int "AB")))
    (t/is (= 52 (sut/AZ->int "AZ")))
    (t/is (= 53 (sut/AZ->int "BA")))
    (t/is (= 702 (sut/AZ->int "ZZ")))
    (t/is (= 703 (sut/AZ->int "AAA"))))
  (t/testing "Converting int to column indexes in ABC notation"
    (t/is (= "A" (sut/int->AZ 1)))
    (t/is (= "B" (sut/int->AZ 2)))
    (t/is (= "Z" (sut/int->AZ 26)))
    (t/is (= "AA" (sut/int->AZ 27)))
    (t/is (= "AB" (sut/int->AZ 28)))
    (t/is (= "AZ" (sut/int->AZ 52)))
    (t/is (= "BA" (sut/int->AZ 53)))
    (t/is (= "ZZ" (sut/int->AZ 702)))
    (t/is (= "AAA" (sut/int->AZ 703)))))

(defspec column-index-conversion-is-reversible
  100
  (prop/for-all [i (gen/such-that #(not= % 0) gen/nat)]
                (= i (sut/AZ->int (sut/int->AZ i)))))

(t/deftest range-parsing
  (t/testing "Parsing ranges from A1 notation"
    (t/is (= {:sheet-name "Sheet1"
              :start-col 1
              :start-row 3
              :end-col 6
              :end-row 6}
             (sut/A1->range "Sheet1!A3:F6"))
          "Parsing a simple range")
    (t/is (= {:sheet-name "Sheet1"
              :start-col 1
              :start-row 3
              :end-col 83
              :end-row 6}
             (sut/A1->range "Sheet1!A3:CE6"))
          "Parsing a simple range with column index >Z")
    (t/is (= {:start-col 1
              :start-row 3
              :end-col 6
              :end-row 6}
             (sut/A1->range "A3:F6"))
          "Parsing a simple range without sheet name")
    (t/is (= {:start-col 1
              :start-row 3}
             (sut/A1->range "A3"))
          "Parsing a single-cell range")
        (t/is (= {:sheet-name "Sheet5"
                  :start-col 17
                  :start-row 34}
                 (sut/A1->range "Sheet5!Q34"))
          "Parsing a single-cell range"))
  (t/testing "Violating constraints"
    (t/is (thrown? RuntimeException
             (sut/A1->range "Sheet1!F3:A6"))
          "Start column greater end column")
    (t/is (thrown? RuntimeException
             (sut/A1->range "Sheet1!A6:F3"))
          "Start row greater end row")))

(t/deftest range-parsing-and-printing
  (t/testing "parsing + printing = identity"
    (t/are [a1] (= a1 (sut/range->A1 (sut/A1->range a1)))
      "Sheet1!A3:F6"
      "Sheet1!A3:CE6"
      "A3:F6")))

(t/deftest range-metrics
  (t/testing "counts"
    (t/is (= 4) (sut/row-count (sut/A1->range "A3:F6")))
    (t/is (= 6) (sut/col-count (sut/A1->range "A3:F6")))))

(t/deftest intersect-ranges
  (t/testing "Ranges with some overlap"
    (t/are [result-a1 a1-1 a1-2] (= result-a1 (sut/range->A1
                                               (sut/intersect
                                                (sut/A1->range a1-1)
                                                (sut/A1->range a1-2))))
      "Sheet1!A3:F6" "Sheet1!A3:F6" "Sheet1!A3:F6"  ; 2 times the same range yields the identity
      "Sheet1!B2:E5" "Sheet1!A1:F6" "Sheet1!B2:E5"  ; range 2 subset of range 1
      "Sheet1!B2:E5" "Sheet1!B2:E5" "Sheet1!A1:F6"  ; range 1 subset of range 2
      "Sheet1!B2:D6" "Sheet1!A1:D6" "Sheet1!B2:F8"  ; some overlap
      )))

(t/deftest cells
  (t/testing "start cell"
    (t/is (= {:sheet-name "Sheet1"
              :col 1
              :row 3}
             (-> "Sheet1!A3:F6"
                 sut/A1->range
                 sut/start-cell))))
  (t/testing "cell to A1"
    (t/is (= "Sheet1!A3"
             (-> "Sheet1!A3:F6"
                 sut/A1->range
                 sut/start-cell
                 sut/cell->A1)))))
