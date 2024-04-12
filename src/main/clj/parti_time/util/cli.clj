(ns parti-time.util.cli)


(defmacro assert-argument
  "assert-argument generalizes argument assertions. It auto-inserts the argument variable name into the failure-message. The failure-message is a java.lang.String.format message containing a %s placeholder for the argument name.

  It's a macro and not a function, because it needs to report the argument name."
  [arg condition failure-message]
  `(let [msg# (format ~failure-message ~(str arg))]
     (when-not (~condition ~arg)
       (throw (RuntimeException. msg#)))))

(defmacro assert-mandatory-argument [arg]
  `(assert-argument ~arg identity "Mandatory argument '%s' not provided."))

(defmacro assert-non-empty-argument [arg]
  `(assert-argument ~arg #(and % (not= % "")) "Argument '%s' must not be empty."))



(defn with-error-printer [command]
  (fn [args]
    (try
      (command args)
      (catch Exception ex
        (binding [*out* *err*]
          (println "An error occured:")
          (println (.toString ex))
          (println (.getMessage ex)))
        1   ; return code for cli-matic
        ))))
