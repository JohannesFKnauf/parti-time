(ns parti-time.output.api)

(defn file-extension [filename timeline]
  (re-find #"(?<=\.)[^.]+$" filename))

(defmulti write-timeline
  file-extension)

(defmethod write-timeline nil [filename timeline]
  (throw (RuntimeException.
          (str "Filename extension for given file '" filename "' could not be detected"))))

(defmethod write-timeline :default [filename timeline]
  (throw (RuntimeException.
          (str "parti-time does not know (yet) how to write filename '" filename "' with extension " (file-extension filename)))))
