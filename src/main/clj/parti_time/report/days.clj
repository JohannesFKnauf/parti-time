(ns parti-time.report.days
  (:require [parti-time.core :as pt]
            [parti-time.util.time :as time])
  (:import [java.time Duration LocalDate LocalDateTime LocalTime]
           [java.time.format DateTimeFormatter]))

(def ^Duration time-grain
  (Duration/ofMinutes 15))


(defn timebits [{:keys [^LocalDateTime start-time
                        ^LocalDateTime end-time
                        project]
                 :as time-window}]
  (cond
    (time/date-time-before? end-time start-time) (throw (RuntimeException. (str "Error trying to create timebits. Wrong offset of the timebit before end time " end-time)))
    (= end-time start-time) nil
    :else (let [new-time-window (update time-window
                                        :start-time
                                        #(.addTo time-grain %))]
            (lazy-seq (cons {:date (.toLocalDate start-time)
                             :project project}
                            (timebits new-time-window))))))

(defn timeline->timebits [timeline]
  (->> timeline
       pt/time-windows
       (map timebits)
       (apply concat)))

(defn project-styles
  "Create a lookup map for project styles.

  As of now, styles are just characters.
  In the future, styles might include optional colors."
  [projects]
  (let [visually-distinctive-chars "._-+xo=nv<>iqp"]   ; the character sequence is deterministic and optimized for visual distinction
                                        ; the alternatives would have been to randomize the characters
                                        ; or to chose characters with a relation to the project names
    (into {} (conj (map vector (sort (disj projects "Private"))
                        visually-distinctive-chars)
                   ["Private" \space]))))

(defn draw-key [styles]
  (map (fn [[project ch]]
         (str ch " " project "\n")) styles))

(defn draw-day [{:keys [^LocalDate date
                        project-bits]
                 :as day}]
  (str (.format date (DateTimeFormatter/ofPattern "yyyy-MM-dd E"))
       "  "
       (apply str (str (clojure.string/join " " (map (partial apply str) (partition-all 4 project-bits)))))
       "\n"))

(defn days-report [timeline]
  (let [^LocalDateTime first-time (:start-time (first timeline))
        start-of-day (fn [^LocalDateTime time]
                       (.with time LocalTime/MIN))
        start-gap-padding {:start-time (start-of-day first-time)
                           :end-time first-time
                           :project "Private"}
        padded-timeline (cons start-gap-padding timeline)
        projects (into #{} (map :project padded-timeline))
        styles (project-styles projects)
        days (->> padded-timeline
                  timeline->timebits
                  (partition-by :date)
                  (map (fn [bits]
                         {:date (:date (first bits))
                          :project-bits (->> bits
                                             (map :project)
                                             (map styles))})))
        drawing-elements {:key (draw-key styles)
                          :days (map draw-day days)}]
    (str
     (clojure.string/join "" (:key drawing-elements))
     "\n"
     ;; numbers for all 3 hours
     (apply str (repeat (+
                         10 ; yyyy-MM-dd
                         1  ; space
                         3)  ; day of week
                        \space))
     (str
      " 0             "
      " 3             "
      " 6             "
      " 9             "
      "12             "
      "15             "
      "18             "
      "21             "
      "24")
     "\n"
     ;; markers for all 3 hours
     (apply str (repeat (+
                         10 ; yyyy-MM-dd
                         1  ; space
                         3  ; day of week
                         1  ; space
                         ) \space))
     (apply str (repeat 8
                        "|              "))
     "|\n"
     ;; markers for hours
     (apply str (repeat (+
                         10 ; yyyy-MM-dd
                         1  ; space
                         3  ; day of week
                         1  ; space
                         ) \space))
     (apply str (repeat 24
                        "|    "))
     "|\n"
     (clojure.string/join "" (:days drawing-elements)))))
