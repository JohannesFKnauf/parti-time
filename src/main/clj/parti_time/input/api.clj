(ns parti-time.input.api)

(defn file-type [format reader-like]
  format)

(defmulti read-timeline-from-reader
  file-type)

(defmethod read-timeline-from-reader :default [format reader-like]
  (throw (RuntimeException.
          (str "parti-time does not know (yet) how to read file format '" format "'"))))

(defn read-timeline [format filename]
    (let [input-reader-like (if (= filename "-")
                              *in*
                              filename)]
    (read-timeline-from-reader format input-reader-like)))
