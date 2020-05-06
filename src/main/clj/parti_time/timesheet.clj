(ns parti-time.timesheet
  (:require [tick.alpha.api :as tick]
            [clojure-csv.core :as csv]
            [parti-time.core]))

(defn day-record [entry]
  (let [occupations (->> entry
                         (:occupation)
                         (flatten))]
    [(tick/format (tick/formatter "yyyy-MM-dd") (tick/date (:start-time entry)))
     (tick/format (tick/formatter "HH:mm") (:start-time entry))
     (tick/format (tick/formatter "HH:mm") (:end-time entry))
     ""
     (clojure.string/join ", " occupations)
     (:project entry)]))

(defn csv-report [time-line]
  (->> time-line
       parti-time.core/time-windows
       (filter #(not= "Private" (:project %1)))
       (map day-record)
       csv/write-csv))
