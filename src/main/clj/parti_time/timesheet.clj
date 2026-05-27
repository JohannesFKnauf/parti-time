(ns parti-time.timesheet
  (:require [parti-time.util.time :as time]
            [clojure-csv.core :as csv]
            [parti-time.core]))

(defn day-row
  "day-row converts a time-window into a row of a timesheet -- for CSV export or sheet upload.

  Requires end-time to be on same day or midnight-next-day -- and won't check it again."
  [{:keys [start-time
           end-time
           occupations
           project]
    :as time-window}]
  [(time/format-time "yyyy-MM-dd" start-time)
   (time/format-time "HH:mm" start-time)
   (if (= end-time (time/start-of-next-day start-time))
     "24:00"
     (time/format-time "HH:mm" end-time))
   ""
   (clojure.string/join ", " (flatten occupations))
   project])

(defn report [time-line]
  (->> time-line
       parti-time.core/time-windows
       (filter #(not (parti-time.core/is-private? %1)))
       (map parti-time.core/split-by-midnight)
       (apply concat)
       (map day-row)))

(defn csv-report [time-line]
  (->> time-line
       report
       csv/write-csv))
