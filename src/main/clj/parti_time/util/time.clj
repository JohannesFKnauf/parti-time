(ns parti-time.util.time
  (:import [java.time.format DateTimeFormatter]
           [java.time Instant LocalDate LocalDateTime LocalTime ZoneId]
           [java.time.temporal ChronoUnit Temporal TemporalAccessor]))

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

(defn minutes-between [^Temporal a
                       ^Temporal b]
  (.between java.time.temporal.ChronoUnit/MINUTES a b))

(defn date [^TemporalAccessor d]
  (LocalDate/from d))

;; Comparing and ordering

(defn date-time-before? [^LocalDateTime a
                         ^LocalDateTime b]
  (.isBefore a b))


;; features concerning absolute date times and conversion to local date times (without time zone information)

(def default-zoneid
  (ZoneId/systemDefault))

(defn zoneid [desc]
  (ZoneId/of desc))

(defn utc->local-date-time
  "Converts a UTC ISO time string in to a LocalDateTime in zone. If zone is omitted, the system default is used."
  ([^String in]
   (utc->local-date-time in default-zoneid))
  ([^String in ^ZoneId zone]
   (-> in
       Instant/parse
       (LocalDateTime/ofInstant zone))))

;; truncating LocalDateTime

(defn truncate-to [unit
                   ^LocalDateTime d]
  (.truncatedTo d 
                (case unit
                  :days ChronoUnit/DAYS
                  :hours ChronoUnit/HOURS
                  :minutes ChronoUnit/MINUTES
                  :seconds ChronoUnit/SECONDS
                  (throw (RuntimeException. (str "Unsupported unit: '" unit "'"))))))
