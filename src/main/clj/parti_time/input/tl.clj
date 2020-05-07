(ns parti-time.input.tl
  (:require [instaparse.core :as insta]
            [parti-time.util.time :as time]
            [parti-time.input.api :as api])
  (:import [java.time LocalDate LocalTime]))

(defn comment? [s]
  (clojure.string/starts-with? s "#"))

(defn strip-comments [s]
  (->> s
       clojure.string/split-lines
       (remove comment?)
       (clojure.string/join "\n")))

(insta/defparser timeline-parser
  "./src/main/resources/tl/timeline_grammar.ebnf")

(defn ast->entries
  "Transforms the abstract syntax tree of a timeline DSL parsing result into a proper timeline."
  ([ast]
   (insta/transform {:reference-date #(hash-map :reference-date (time/parse-date "yyyy-MM-dd" %))
                     :hhmm-time #(hash-map :time (time/parse-time "HHmm" %))
                     :subject #(hash-map :subject %)
                     :detail identity
                     :details #(hash-map :details (apply vector %&))
                     :entry merge
                     :timeline vector}
                    ast)))

(defn entry->timeslice [{:keys [^LocalDate reference-date
                                ^LocalTime time
                                subject
                                details] :or {details []}}]
  {:start-time (.atTime reference-date time)
   :project subject
   :occupation details})

(defn fill-in-missing-reference-dates
  ([entries]
   (if-let [initial-reference-date (-> entries first :reference-date)]
     (fill-in-missing-reference-dates entries
                                      initial-reference-date)
     (throw (RuntimeException. "Mandatory reference date missing for the first entry in series of entries."))))
  ([entries prev-reference-date]
   (lazy-seq
    (when-let [entries-seq (seq entries)]
      (let [cur-entry (first entries-seq)
            remaining-entries (rest entries-seq)
            effective-reference-date (get cur-entry :reference-date prev-reference-date)
            full-entry (assoc cur-entry :reference-date effective-reference-date)]
        (cons full-entry (fill-in-missing-reference-dates remaining-entries
                                                          effective-reference-date)))))))

(defn import-timeline [tl-timeline]
  (->> tl-timeline
       strip-comments
       timeline-parser
       ast->entries
       fill-in-missing-reference-dates
       (map entry->timeslice)))

(defn read-timeline [filename]
  (-> filename
      slurp
      import-timeline))

(defmethod api/read-timeline "tl" [filename]
  (read-timeline filename))
