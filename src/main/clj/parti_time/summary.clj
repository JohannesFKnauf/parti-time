(ns parti-time.summary
  (:require [parti-time.core]
            [medley.core]))

(defn duration-hours [time-windows]
  (->> time-windows
       (map :duration-minutes)
       (reduce +)
       (#(/ % 60.0))))

(defn project-summary [time-line]
  (->> time-line
       parti-time.core/time-windows
       (group-by :project)
       (medley.core/map-vals duration-hours)))
