(ns parti-time.google-sheets.timeline
  (:import [java.time LocalDateTime])
  (:require [parti-time.core]
            [parti-time.timesheet]
            [parti-time.google-sheets.client]
            [parti-time.google-sheets.ranges]
            [parti-time.util.time]))

(def standard-header-row
  "The header row -- as defined by the metamorphant timesheet
  schema. Yes, it has german field names."
  ["Datum" "Von" "Bis" "Summe" "Tätigkeit" "Arbeitspaket"])

(defn assert-equals
  [actual
   expected]
  (when-not (= actual expected)
    (throw (RuntimeException. (format "Assertion failed: Value '%s' does not equal expected value '%s'." actual expected)))))

(defn assert-non-blank
  [value]
  (when (clojure.string/blank? value)
    (throw (RuntimeException. (format "Assertion failed: Value '%s' is blank." value)))))

(defn check-headers
  [google-sheet-id]
  (let [[[title name]
         empty
         header-row] (:values (parti-time.google-sheets.client/get-cells google-sheet-id "A1:F3"))]
    (assert-equals title "Zeitnachweis")
    (assert-non-blank name)
    (assert-equals empty [])
    (assert-equals header-row standard-header-row)))


(defn row->time-window [[date from-time to-time duration occupations project :as row]]
  (try
    (let [base-date (parti-time.util.time/parse-iso-date date)
          time-window {:start-time (LocalDateTime/of base-date
                                                     (parti-time.util.time/parse-iso-time from-time))
                       :end-time (LocalDateTime/of base-date
                                                   (parti-time.util.time/parse-iso-time to-time))
                       :occupations (as-> occupations o
                                      (clojure.string/split o #",")
                                      (map clojure.string/trim o)
                                      (filter (complement clojure.string/blank?) o))
                       :project project}]
      time-window)
    (catch Exception ex (throw (RuntimeException. (str "Failed to parse row: " row) ex)))))

(defn google-sheet->timeline [google-sheet-id]
  (let [{raw-timesheet :values
         range :range} (parti-time.google-sheets.client/get-cells google-sheet-id "A:F")
        timesheet-with-header-row (drop-while (partial not= standard-header-row) raw-timesheet)
        timesheet (drop 1 timesheet-with-header-row)
        time-windows (map row->time-window timesheet)]
    ;; converting to a time-line also verifies, that the time windows are ordered and overlap-free
    (parti-time.core/time-windows->time-line time-windows)))

(defn format-timesheet-range!
  "Apply formatting and inject default formulas into a Google Sheet time sheet."
  [google-sheet-id
   range]
  {:pre [(contains? range :start-col)
         (contains? range :end-col)
         (contains? range :start-row)
         (contains? range :end-row)
         (= (:start-col range) 1)  ; A
         (= (:end-col range) 6)]}  ; F
  (let [date-column (assoc range :start-col 1 :end-col 1)
        start-times (assoc range :start-col 2 :end-col 2)
        end-times (assoc range :start-col 3 :end-col 3)
        durations (assoc range :start-col 4 :end-col 4)
        duration-start-cell (parti-time.google-sheets.ranges/start-cell durations)
        reference-end-time (-> duration-start-cell
                               (assoc :col 3)
                               parti-time.google-sheets.ranges/cell->A1)
        reference-start-time (-> duration-start-cell
                                 (assoc :col 2)
                                 parti-time.google-sheets.ranges/cell->A1)
        duration-formula (str "=" reference-end-time "-" reference-start-time)]
    (parti-time.google-sheets.client/set-formula google-sheet-id
                                                 durations
                                                 duration-formula)
    (parti-time.google-sheets.client/set-number-format google-sheet-id
                                                       durations
                                                       "TIME"
                                                       "[h]:mm")
    (parti-time.google-sheets.client/set-number-format google-sheet-id
                                                       date-column
                                                       "DATE"
                                                       "yyyy-mm-dd")
    (parti-time.google-sheets.client/set-number-format google-sheet-id
                                                       start-times
                                                       "TIME"
                                                       "hh:mm")
    (parti-time.google-sheets.client/set-number-format google-sheet-id
                                                       end-times
                                                       "TIME"
                                                       "hh:mm")
    :success))

(defn append-timeline!
  "Append a timeline to a google-sheet.

  This is a lossy, since all time-windows with project Private are
  dropped. Also, joining occupations by comma is not reversable, since
  an occupation can contain a comma itself (it's just discouraged, but
  not forbidden)."
  [google-sheet-id
   timeline]
  (check-headers google-sheet-id)
  (let [{[last-row] :values} (parti-time.google-sheets.client/get-last-row google-sheet-id "A:F")
        not-same-row? (fn [row1 row2]
                        (not= (row->time-window row1)
                              (row->time-window row2)))
        report (parti-time.timesheet/report timeline)
        new-report (->> report
                        (drop-while (partial not-same-row? last-row))
                        (drop 1))
        new-report-rows-count (count new-report)]
    (if (= 0 new-report-rows-count)
      {:updated-rows-count 0
       :updated-rows-content []}
      (let [updated-range-A1 (parti-time.google-sheets.client/append-rows google-sheet-id new-report "A:F")
            updated-range (parti-time.google-sheets.ranges/A1->range updated-range-A1)
            updated-rows-count (parti-time.google-sheets.ranges/row-count updated-range)]
        (assert (= new-report-rows-count updated-rows-count))
        (format-timesheet-range! google-sheet-id updated-range)
        {:updated-rows-count updated-rows-count
         :updated-rows-content new-report}))))


;; Known limitation: Does not remove and reset the filter on the sheet

;; Known limitation: Poor error messages, right now we don't keep context of sheet lines and print it in case of an error nor do we print more information about the data with the error e.g. the detailed time string that fails to be parsed

;; Known limitation: The program stops at the first error discovered when interpreting an existing sheet instead of using an error collection pattern on validation and returning all validation errors at once

;; Known limitation: Input validation is not very exhaustive

;; Known limitation: row->time-window doesn't validate that project isn't empty
