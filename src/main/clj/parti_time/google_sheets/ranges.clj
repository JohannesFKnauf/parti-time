(ns parti-time.google-sheets.ranges)

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


(defn A1->range [a1]
  (let [m (re-matcher #"((?<sheetName>[^!]*)!)?(?<startColumn>[A-Z]*)(?<startRow>[0-9]*)(:(?<endColumn>[A-Z]*)(?<endRow>[0-9]*))?" a1)
        _ (re-find m)
        range {:sheet-name (.group m "sheetName")
               :start-col (AZ->int (.group m "startColumn"))
               :start-row (Integer/parseInt (.group m "startRow"))
               :end-col (AZ->int (or (.group m "endColumn")
                                     (.group m "startColumn")))
               :end-row (Integer/parseInt (or (.group m "endRow")
                                              (.group m "startRow")))}]
    (when (< (:end-col range) (:start-col range))
      (throw (RuntimeException. (str "end column '" (:end-col range) "' is smaller than start column '" (:start-col range) "'"))))
    (when (< (:end-row range) (:start-row range))
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
  (- (:end-row range)
     (:start-row range)))

(defn col-count [range]
  (- (:end-col range)
     (:start-col range)))

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


;; Known limitation: intersect does not properly check for non-overlapping ranges

;; Known limitation: the range parsing/printing does not properly support several special cases, e.g. no sheet, "A:F" without row numbers, "A3" single fields, ...
