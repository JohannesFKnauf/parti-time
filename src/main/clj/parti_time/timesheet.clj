(ns parti-time.timesheet
  (:require [parti-time.util.time :as time]
            [clojure-csv.core :as csv]
            [parti-time.core]))


(defn split-into-days [{:keys [start-time
                               end-time]
                        :as time-window}]
  (lazy-seq
   (let [next-day (time/start-of-next-day start-time)]
     (if (time/date-time-after? end-time next-day)
       (let [first-day-time-window (assoc time-window
                                          :duration-minutes (time/minutes-between start-time next-day)
                                          :end-time next-day)
             remaining-time-window (assoc time-window
                                          :duration-minutes (time/minutes-between next-day end-time)
                                          :start-time next-day)]
         (cons first-day-time-window
               (split-into-days remaining-time-window)))
       (list time-window)))))

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
       (map split-into-days)
       (apply concat)
       (map day-row)))

(defn csv-report [time-line]
  (->> time-line
       report
       csv/write-csv))
