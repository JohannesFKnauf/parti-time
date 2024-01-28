(ns parti-time.output.api)

(defn file-extension [filename timeline]
  (or (re-find #"(?<=\.)[^.]+$" filename)
      "tl"))

(defmulti write-timeline
  file-extension)

(defmethod write-timeline :default [filename timeline]
  (throw (RuntimeException.
          (str "parti-time does not know (yet) how to write filename '" filename "' with extension " (file-extension filename)))))
