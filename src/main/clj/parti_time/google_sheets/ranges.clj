(ns parti-time.google-sheets.ranges
  (:require [instaparse.core :as insta]
            [parti-time.util.instaparse :as instautil]))

(def offset-A 64) ;; \A = 65 but \A should map to 1 instead of to 0

(def powers-base-26
  (cons 1 (iterate (partial * 26) 26)))

(defn A->int
  "Convert the digits A--Z to the corresponding integers 1--26."
  [a]
  (- (int a) offset-A))

(defn int->A
  "Convert the integers 1--26 to the corresponding digits A--Z."
  [i]
  (char (+ i offset-A)))

(defn AZ->int
  "Convert a spreadsheet column index into an integer representation.

  A spreadsheet column index can be considered a bijective base-26
  numeration. The notation works without a zero digit. Instead, the A
  digit represents a 1 and the highest available digit Z represents
  26 (= the base).

  https://en.wikipedia.org/wiki/Bijective_numeration#The_bijective_base-26_system

  See the tests for examples."
  [az]
  (->> az
       (reverse)
       (map A->int)
       (map * powers-base-26)
       (reduce +)))

(defn reverse-digits [n]
  (lazy-seq (if (zero? n) '()
                (let [r (rem n 26)
                      q (quot n 26)
                      [digit leftover] (if (zero? r)
                                         [26 (dec q)]
                                         [r q])]
                  (cons digit
                        (reverse-digits leftover))))))

(defn reverse-digits-2
  "Alternative, equivalent implementation of reverse-digits.

  Included for documentation purposes."
  [n]
  (lazy-seq (if (zero? n) '()
                (let [r (rem (- n 1) 26)
                      q (quot (- n r 1) 26)]
                  (cons (+ r 1)
                        (reverse-digits q))))))

(defn int->AZ
  "Convert an integer into a spreadsheet column index.

  See the tests for examples."
  [n]
  (if (zero? n) '(0)
      (->> n
           (reverse-digits)
           (reverse)
           (map int->A)
           (apply str))))


(insta/defparser a1-parser
  "./src/main/ebnf/a1_notation_grammar.ebnf")

;(instautil/throw-parse-errors parser-result)

(defn a1-ast->raw-range [parser-result]
  (insta/transform {:sheet-name #(hash-map :sheet-name %)
                    :row #(hash-map :row (Integer/parseInt %))
                    :col #(hash-map :col (AZ->int %))
                    :start-cell (comp #(clojure.set/rename-keys
                                        %
                                        {:row :start-row
                                         :col :start-col})
                                      merge)
                    :end-cell (comp #(clojure.set/rename-keys
                                      %
                                      {:row :end-row
                                       :col :end-col})
                                    merge)
                    :a1 merge}
                   parser-result))

(defn A1->range
  "Converts a range in A1 notation to a range map representation.

  Please note, that the representation is not equivalent to Google's Sheets API range JSON representation
  https://developers.google.com/workspace/sheets/api/reference/rest/v4/spreadsheets/other#GridRange.
  Google uses
   - sheetId (numeric),
   - startRowIndex (inclusive, starting with 0),
   - endRowIndex (exclusive, starting with 0),
   - startColumnIndex (inclusive, starting with 0),
   - endColumnIndex (exclusive, starting with 0).

  We use
   - different names,
   - inclusive ends and
   - we start counting with 1.

  Any missing element in the A1 notation will be missing in the resulting hash-map.
  E.g. a missing sheet name prefix (think A1:B2) means there won't be a :sheet-name key.
  E.g. a missing end of the range (think Sheet1!A3) means there won't be :end-col and :end-row keys.
  E.g. an open-ended range without rows (think Sheet1!A:F) means there won't be :start-row and :end-row keys."
  [a1]
  (let [range (-> a1
                  a1-parser
                  instautil/throw-parse-errors
                  a1-ast->raw-range)]
    (when (and (contains? range :end-col)
               (contains? range :start-col)
               (< (:end-col range) (:start-col range)))
      (throw (RuntimeException. (str "end column '" (:end-col range) "' is smaller than start column '" (:start-col range) "'"))))
    (when (and (contains? range :end-row)
               (contains? range :start-row)
               (< (:end-row range) (:start-row range)))
      (throw (RuntimeException. (str "end row '" (:end-row range) "' is smaller than start row '" (:start-row range) "'"))))
    range))

(defn range->A1 [range]
  (str (when-let [sn (:sheet-name range)]
         (str sn "!"))
       (int->AZ (:start-col range))
       (:start-row range)
       ":"
       (int->AZ (:end-col range))
       (:end-row range)))

(defn row-count [range]
  (+ 1 
     (- (:end-row range)
        (:start-row range))))

(defn col-count [range]
  (+ 1
     (- (:end-col range)
        (:start-col range))))

(defn intersect
  "Intersect two ranges, i.e. get the subrange that's included in both ranges."
  [{sheet-name-1 :sheet-name
    start-col-1 :start-col
    start-row-1 :start-row
    end-col-1 :end-col
    end-row-1 :end-row}
   {sheet-name-2 :sheet-name
    start-col-2 :start-col
    start-row-2 :start-row
    end-col-2 :end-col
    end-row-2 :end-row}]
  (when-not (= sheet-name-1 sheet-name-2)
    (throw (RuntimeException. "The sheet names of the 2 ranges don't match.")))
  {:sheet-name sheet-name-1
   :start-col (max start-col-1 start-col-2)
   :start-row (max start-row-1 start-row-2)
   :end-col (min end-col-1 end-col-2)
   :end-row (min end-row-1 end-row-2)})


(defn start-cell
  [{:keys [sheet-name
           start-col
           start-row]}]
  {:sheet-name sheet-name
   :col start-col
   :row start-row})

(defn cell->A1
  [{:keys [sheet-name
           col
           row]}]
  (str (when-let [sn sheet-name]
         (str sn "!"))
       (int->AZ col)
       row))


;; Known limitation: intersect does not properly check for non-overlapping ranges

;; Known limitation: the range parsing/printing does not properly support several special cases, e.g. no sheet, "A:F" without row numbers (i.e. open-ended ranges), "A3" single fields, single quotes around sheet names, trailing extra letters, ...
;; -> minimum: have test cases documenting the (insufficient) current behaviour
