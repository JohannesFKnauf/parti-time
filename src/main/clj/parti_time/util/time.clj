(ns parti-time.util.time
  (:import [java.time.format DateTimeFormatter]
           [java.time LocalDate LocalDateTime LocalTime]
           [java.time.temporal Temporal TemporalAccessor]))

(defn formatter ^DateTimeFormatter [^String fmt]
  (DateTimeFormatter/ofPattern fmt))

(defn format-time [^String fmt
                   ^TemporalAccessor ta]
  (.format (formatter fmt) ta))
                  
(defn parse-date [^String fmt
                  ^String in]
  (LocalDate/parse in (formatter fmt)))

(defn parse-time [^String fmt
                  ^String in]
  (LocalTime/parse in (formatter fmt)))

(defn parse-iso-date [^String in]
  (LocalDate/parse in DateTimeFormatter/ISO_LOCAL_DATE))

(defn parse-iso-time [^String in]
  (LocalTime/parse in DateTimeFormatter/ISO_TIME))

(defn parse-iso-date-time [^String in]
  (LocalDateTime/parse in DateTimeFormatter/ISO_LOCAL_DATE_TIME))

(defn date-time-before? [^LocalDateTime a
                         ^LocalDateTime b]
  (.isBefore a b))

(defn minutes-between [^Temporal a
                       ^Temporal b]
  (.between java.time.temporal.ChronoUnit/MINUTES a b))

(defn date [^TemporalAccessor d]
  (LocalDate/from d))
