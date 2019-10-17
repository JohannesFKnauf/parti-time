(ns parti-time.input.api)


(defn file-extension [filename]
  (re-find #"(?<=\.)[^.]+$" filename))

(defmulti read-timeline
  file-extension)

(defmethod read-timeline nil [filename]
  (throw (RuntimeException.
          (str "Filename extension for given file '" filename "' could not be detected"))))

(defmethod read-timeline :default [filename]
  (throw (RuntimeException.
          (str "parti-time does not know (yet) how to read filename '" filename "' with extension " (file-extension filename)))))
