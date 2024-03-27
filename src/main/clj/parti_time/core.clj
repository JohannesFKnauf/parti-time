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

(defn gen-time-slices [time-window next-time-window]
  (when (time/date-time-before? (:end-time time-window) (:start-time time-window))
    (throw (java.lang.IllegalArgumentException. (str "End time '" (:start-time next-time-window) "' predates start time '" (:end-time time-window) "'. "))))
  (when (and (some? next-time-window)
             (time/date-time-before? (:start-time next-time-window) (:end-time time-window)))
    (throw (java.lang.IllegalArgumentException. (str "Subsequent start time '" (:start-time next-time-window) "' predates previous end time '" (:end-time time-window) "'. "))))
  (into
   [(dissoc time-window :end-time)]
   ; fill up gaps with Private time-windows
   (when (not= (:start-time next-time-window) (:end-time time-window))
     [{:start-time (:end-time time-window)
       :project "Private"}])))


(defn time-windows->time-line [time-windows]
  (apply concat
         (mapv gen-time-slices
               time-windows
               (concat (drop 1 time-windows) [nil]))))
