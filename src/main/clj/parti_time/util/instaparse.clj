(ns parti-time.util.instaparse
  (:require [instaparse.core :as insta]))

(defn get-parse-error ^String [parser-failure]
  (with-out-str
    (-> parser-failure
        insta/get-failure
        print)))

(defn throw-parse-errors [parser-result]
  (when (insta/failure? parser-result)
    (throw (RuntimeException. (get-parse-error parser-result))))
  parser-result)
