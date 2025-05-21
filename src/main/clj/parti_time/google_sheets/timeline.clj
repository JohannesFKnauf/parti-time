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
        updated-range (parti-time.google-sheets.client/append-rows google-sheet-id new-report "A:F")]
    {:updated-rows-count (-> updated-range
                             parti-time.google-sheets.ranges/A1->range
                             parti-time.google-sheets.ranges/row-count)
     :updated-rows-content new-report}))


;; Known limitation: appending doesn't force-format all appended rows
;;  - column A: DATE with pattern yyyy-mm-dd
;;  - column B+C: TIME with pattern hh:mm
;;  - column D: formula =C-B and format custom number [hh]:mm (effectively duration)
;;              how to fix the quicksum and prevent it from wrapping sums >24h?
;;                -> add a fixed value instead of a formula?
;;  - column E: wrap around

;; Known limitation: Does not report details about the success, e.g. number of appended rows.

;; Known limitation: Does not remove and reset the filter on the sheet

;; Known limitation: Poor error messages, right now we don't keep context of sheet lines and print it in case of an error nor do we print more information about the data with the error e.g. the detailed time string that fails to be parsed
;; Known limitation: The program stops at the first error discovered when interpreting an existing sheet instead of using an error collection pattern on validation and returning all validation errors at once

;; Known limitation: Input validation is not very exhaustive

;; Known limitation: row->time-window doesn't validate that project isn't empty
