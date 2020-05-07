(ns parti-time.util.time
  (:import [java.time.format DateTimeFormatter]
           [java.time LocalDate LocalDateTime LocalTime]))

(defn formatter [^String fmt]
  (DateTimeFormatter/ofPattern fmt))

(defn format-date [^String fmt
                   ^LocalDate d]
  (.format d (formatter fmt)))

(defn format-time [^String fmt
                   ^LocalTime d]
  (.format d (formatter fmt)))

(defn format-date-time [^String fmt
                        ^LocalDateTime d]
  (.format d (formatter fmt)))

(defn date [^LocalDateTime time]
  ())
                  
(defn parse-date [^String fmt
                  ^String in]
  (LocalDate/parse in (formatter fmt)))

(defn parse-time [^String fmt
                  ^String in]
  (LocalTime/parse in (formatter fmt)))

(defn parse-iso-date-time [^String in]
  (LocalDateTime/parse in DateTimeFormatter/ISO_LOCAL_DATE_TIME))

(defn date-time-before? [^LocalDateTime a
                         ^LocalDateTime b]
  (.isBefore a b))

(defn minutes-between [^LocalDateTime a
                       ^LocalDateTime b]
  (.between java.time.temporal.ChronoUnit/MINUTES a b))

(defn date-time->date [^LocalDateTime d]
  (LocalDate/from d))
