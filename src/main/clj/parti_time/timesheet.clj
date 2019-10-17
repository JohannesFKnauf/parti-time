(ns parti-time.timesheet
  (:require [java-time]
            [clojure-csv.core :as csv]
            [parti-time.core]))

(defn day-record [entry]
  (let [occupations (->> entry
                         (:occupation)
                         (flatten))]
    [(java-time/format "yyyy-MM-dd" (java-time/local-date (:start-time entry)))
     (java-time/format "HH:mm" (:start-time entry))
     (java-time/format "HH:mm" (:end-time entry))
     ""
     (clojure.string/join ", " occupations)
     (:project entry)]))

(defn csv-report [time-line]
  (->> time-line
       parti-time.core/time-windows
       (filter #(not= "Private" (:project %1)))
       (map day-record)
       csv/write-csv))
