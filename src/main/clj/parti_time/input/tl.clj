(ns parti-time.input.tl
  (:require [instaparse.core :as insta]
            [parti-time.util.time :as time]
            [parti-time.input.api :as api])
  (:import [java.time LocalDate LocalTime]))

(insta/defparser timeline-parser
  "./src/main/resources/tl/timeline_grammar.ebnf")

(defn get-parse-error ^String [parser-failure]
  (with-out-str
    (-> parser-failure
        insta/get-failure
        print)))

(defn throw-parse-errors [parser-result]
  (when (insta/failure? parser-result)
    (throw (RuntimeException. (get-parse-error parser-result))))
  parser-result)

(defn entry->timeslice [^LocalDate reference-date
                        {:keys [^LocalTime time
                                project
                                activities] :or {activities []}}]
  {:start-time (.atTime reference-date time)
   :project project
   :occupation activities})

(defn ast->entries
  "Transforms the abstract syntax tree of a timeline DSL parsing result into a proper timeline."
  ([ast]
   (insta/transform {:reference-date #(time/parse-date "yyyy-MM-dd" %)
                     :hhmm-time #(hash-map :time (time/parse-time "HHmm" %))
                     :project #(hash-map :project %)
                     :activity identity
                     :activities #(hash-map :activities (apply vector %&))
                     :entry merge
                     :entries vector
                     :day #(map (partial entry->timeslice %1) %2)   ; %1 is the reference-date, %2 are the entries
                     :timeline concat}
                    ast)))

(defn import-timeline [tl-timeline]
  (->> tl-timeline
       timeline-parser
       throw-parse-errors
       ast->entries))

(defn read-timeline [filename]
  (-> filename
      slurp
      import-timeline))

(defmethod api/read-timeline "tl" [filename]
  (read-timeline filename))
