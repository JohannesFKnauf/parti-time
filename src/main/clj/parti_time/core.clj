(ns parti-time.core
  (:require [parti-time.util.time :as time]))

(defn time-window [time-frame next-time-frame]
  (let [{start-time :start-time} time-frame
        {end-time :start-time} next-time-frame]
    (when (time/date-time-before? end-time start-time)
      (throw (java.lang.IllegalArgumentException. (str "End time '" end-time "' predates start time '" start-time "'. Times must be strictly ordered."))))
    (assoc time-frame
           :end-time end-time
           :duration-minutes (time/minutes-between start-time end-time))))

(defn time-windows [time-line]
  (mapv time-window
        time-line
        (drop 1 time-line)))
