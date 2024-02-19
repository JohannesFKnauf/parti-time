(ns parti-time.input.api)

(defn file-type [format filename]
  format)

(defmulti read-timeline
  file-type)

(defmethod read-timeline :default [format filename]
  (throw (RuntimeException.
          (str "parti-time does not know (yet) how to read file format '" format "'"))))
