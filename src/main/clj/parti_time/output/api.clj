(ns parti-time.output.api)

(defn file-type [format writer-like timeline]
  format)

(defmulti write-timeline-to-writer!
  file-type)

(defmethod write-timeline-to-writer! :default [format writer-like timeline]
  (throw (RuntimeException.
          (str "parti-time does not know (yet) how to write file format '" format "'"))))

(defn write-timeline [format filename timeline]
  (let [output-writer-like (if (= filename "-")
                              *out*
                              filename)]
    (write-timeline-to-writer! format output-writer-like timeline)))
