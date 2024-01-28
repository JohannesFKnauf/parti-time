(ns parti-time.input.api)


(defn file-extension [filename]
  (or (re-find #"(?<=\.)[^.]+$" filename)
      "tl"))

(defmulti read-timeline
  file-extension)

(defmethod read-timeline :default [filename]
  (throw (RuntimeException.
          (str "parti-time does not know (yet) how to read filename '" filename "' with extension " (file-extension filename)))))
