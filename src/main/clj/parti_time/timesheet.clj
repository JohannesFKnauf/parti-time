(ns parti-time.timesheet
  (:require [parti-time.util.time :as time]
            [clojure-csv.core :as csv]
            [parti-time.core]))

(defn day-record [entry]
  (let [occupations (->> entry
                         (:occupations)
                         (flatten))]
    [(time/format-time "yyyy-MM-dd" (:start-time entry))
     (time/format-time "HH:mm" (:start-time entry))
     (time/format-time "HH:mm" (:end-time entry))
     ""
     (clojure.string/join ", " occupations)
     (:project entry)]))

(defn csv-report [time-line]
  (->> time-line
       parti-time.core/time-windows
       (filter #(not= "Private" (:project %1)))
       (map day-record)
       csv/write-csv))
