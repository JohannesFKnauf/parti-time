(ns parti-time.output.tl
  (:require [parti-time.util.time :as time]
            [parti-time.output.api :as api]))


(defn export-day [start-time previous-start-time]
  (cond
    (nil? previous-start-time) (str (time/format-time "yyyy-MM-dd" start-time) "\n")
    (= (time/date previous-start-time) (time/date start-time)) ""
    :else (str "\n"
               (time/format-time "yyyy-MM-dd" start-time) "\n")))

(defn export-time-and-project [start-time project]
  (str (time/format-time "HHmm" start-time) " " project "\n"))

(defn export-occupation [occupation]
  (str "     " occupation "\n"))

(defn export-occupations [occupations]
  (apply str (map export-occupation occupations)))

(defn export-entry [entry previous-entry]
  (let [{:keys [start-time
                project
                occupations]} entry
        {previous-start-time :start-time} previous-entry]
    (str (export-day start-time previous-start-time)
         (export-time-and-project start-time project)
         (export-occupations occupations))))

(defn export-timeline [timeline]
  (apply str (map export-entry timeline (cons nil timeline))))

(defn write-timeline [filename timeline]
  (->> timeline
       export-timeline
       (spit filename)))
  
(defmethod api/write-timeline "tl" [format filename timeline]
  (if (= filename "-")
    (write-timeline *out* timeline))
    (write-timeline filename timeline))
