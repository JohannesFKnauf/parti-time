(ns parti-time.invoice-report
  (:require [parti-time.util.time :as time]
            [clojure-csv.core :as csv]
            [parti-time.core]))

(defn if-cmp [cmp-fn a b]
  (if (cmp-fn a b)
    a
    b))

(defn cmp-time [cmp-fn]
  (partial if-cmp cmp-fn))

(def min-time
  (cmp-time time/date-time-before?))

(def max-time
  (cmp-time (complement time/date-time-before?)))

(defn format-duration [minutes]
  (->> [minutes 60]
       (apply (juxt quot rem))
       (apply (partial format "%02d:%02d"))))

(defn day-record [[date day-entries]]
  (let [business-day-start-time (->> day-entries
                                     (map :start-time)
                                     (reduce min-time))
        business-day-end-time (->> day-entries
                                   (map :end-time)
                                   (reduce max-time))
        business-minutes (time/minutes-between business-day-start-time business-day-end-time)
        work-minutes (->> day-entries
                        (map :duration-minutes)
                        (reduce +))
        break-minutes (- business-minutes work-minutes)
        occupations (->> day-entries
                         (map :occupation)
                         (filter #(not (= [""] %)))
                         (flatten))]
    [(time/format-time "yyyy-MM-dd" date)
     (time/format-time "HH:mm" business-day-start-time)
     (time/format-time "HH:mm" business-day-end-time)
     (format-duration break-minutes)
     (format-duration work-minutes)
     (clojure.string/join ", " occupations)]))


(defn matches-project? [project-name {:keys [project]}]
  (= project-name project))

(defn csv-report [time-line project-name]
  (let [records (parti-time.core/time-windows time-line)
        project-records (filter #(matches-project? project-name %) records)
        day-entries (group-by #(time/date (:start-time %1)) project-records)
        day-records (map day-record day-entries)]
    (csv/write-csv day-records)))
