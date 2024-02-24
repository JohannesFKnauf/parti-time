; tt json is the JSON format used by https://github.com/dribnif/tt/
(ns parti-time.input.tt
  (:require [clojure.data.json :as json]
            [parti-time.util.time :as time]
            [parti-time.input.api :as api]))

(defn assert-mandatory-field [object field]
  (if (not (contains? object field))
    (throw (RuntimeException. (str "Mandatory field '" field "' is missing in object '" object "'")))
    object))

(defn assert-mandatory-fields [entry]
  (-> entry
      (assert-mandatory-field :start)
      (assert-mandatory-field :end)
      (assert-mandatory-field :name)))

(defn assert-local-date-time-field [object field]
  (if (not (instance? java.time.LocalDateTime (field object)))
    (throw (RuntimeException. (str "'" field "' time '" (field object) "' is not a valid LocalDateTime.")))
    object))

(defn assert-start-before-end [entry]
  (if (time/date-time-before? (:end entry) (:start entry))
    (throw (RuntimeException. (str "End time '" (:end entry) "' is before start time '" (:start entry) "'")))
    entry))

(defn assert-valid-start-and-end-time [entry]
  (-> entry
      (assert-local-date-time-field :start)
      (assert-local-date-time-field :end) ;; While open entries are technically possible in tt, we forbid them
      (assert-start-before-end)))

(defn interpret-entry
  "Reads a worklog entry and coerces the fields to proper types."
  [entry]
  (-> entry
      (assert-mandatory-fields)
      (dissoc :tags) ; tags are a dead and unused feature in tt, according to its author dribnif
      (update :start (comp (partial time/truncate-to :minutes)
                           time/utc->local-date-time))
      (update :end (comp (partial time/truncate-to :minutes)
                         time/utc->local-date-time))
      (assert-valid-start-and-end-time)))

(defn emit-timeslices
  [entry next-entry]
  (when (and (not (nil? next-entry))
           (time/date-time-before? (:start next-entry) (:end entry)))
    (throw (RuntimeException. (str "Overlapping entries detected. Start time '" (:start next-entry) "' is before end time '" (:end entry) "' of previous entry."))))
  (cons {:start-time (:start entry)
         :project (:name entry)
         :occupations (:notes entry)}
        (when (not= (:start next-entry) (:end entry)) ; if there is a gap, fill it with Private time
          [{:start-time (:end entry)
            :project "Private"
            :occupations []}])))

(defn tt-worklog->timeline [tt-worklog]
  (->> tt-worklog
       :work
       (map interpret-entry)
       (sort-by :start)                          ; establish time-ordering
       (partition 2 1 [nil])                     ; create a seq of pairs [entry next-entry], fill last pair with nil as next-entry
       (map (partial apply emit-timeslices))     ; convert each entry to 1..2 timeslices
       (apply concat)))                          ; flatten the list of 1..2 timeslices

(defn import-timeline
  "Returns the timeline described by the JSON string tt-json."
  [tt-json]
  (-> tt-json
      (json/read-str :key-fn keyword)
      tt-worklog->timeline))

(defn read-timeline [filename]
  (-> filename
      slurp
      import-timeline))

(defmethod api/read-timeline "tt" [format filename]
  (if (= filename "-")
    (read-timeline *in*)
    (read-timeline filename)))
