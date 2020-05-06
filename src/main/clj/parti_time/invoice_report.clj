(ns parti-time.invoice-report
  (:require [tick.alpha.api :as tick]
            [clojure-csv.core :as csv]
            [parti-time.core]))

(defn if-cmp [cmp-fn a b]
  (if (cmp-fn a b)
    a
    b))

(defn cmp-time [cmp-fn]
  (partial if-cmp cmp-fn))

(def min-time
  (cmp-time tick/<))

(def max-time
  (cmp-time tick/>))

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
        business-minutes (tick/minutes (tick/between business-day-start-time business-day-end-time))
        work-minutes (->> day-entries
                        (map :duration-minutes)
                        (reduce +))
        break-minutes (- business-minutes work-minutes)
        occupations (->> day-entries
                         (map :occupation)
                         (filter #(not (= [""] %)))
                         (flatten))]
    [(tick/format (tick/formatter "yyyy-MM-dd") date)
     (tick/format (tick/formatter "HH:mm") business-day-start-time)
     (tick/format (tick/formatter "HH:mm") business-day-end-time)
     (format-duration break-minutes)
     (format-duration work-minutes)
     (clojure.string/join ", " occupations)]))


(defn matches-project? [project-name {:keys [project]}]
  (= project-name project))

(defn csv-report [time-line project-name]
  (let [records (parti-time.core/time-windows time-line)
        project-records (filter #(matches-project? project-name %) records)
        day-entries (group-by #(tick/date (:start-time %1)) project-records)
        day-records (map day-record day-entries)]
    (csv/write-csv day-records)))
