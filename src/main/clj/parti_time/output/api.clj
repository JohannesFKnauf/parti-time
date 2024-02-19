(ns parti-time.output.api)

(defn file-type [format filename timeline]
  format)

(defmulti write-timeline
  file-type)

(defmethod write-timeline :default [format filename timeline]
  (throw (RuntimeException.
          (str "parti-time does not know (yet) how to write file format '" format "'"))))
