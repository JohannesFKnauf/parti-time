(ns parti-time.util.cli-test
  (:require [clojure.test :as t]
            [parti-time.util.cli :as sut]))

(t/deftest mandatory-arguments
  (t/is (thrown-with-msg? RuntimeException #"Mandatory argument 'some-arg' not provided."
                          (let [some-arg nil]
                            (sut/assert-mandatory-argument some-arg))))
  (t/is (= nil (let [some-arg "some-value"]
                 (sut/assert-mandatory-argument some-arg)))))

(t/deftest non-empty-arguments
  (t/is (thrown-with-msg? RuntimeException #"Argument 'some-arg' must not be empty."
                          (let [some-arg nil]
                            (sut/assert-non-empty-argument some-arg))))
  (t/is (thrown-with-msg? RuntimeException #"Argument 'some-arg' must not be empty."
                          (let [some-arg ""]
                            (sut/assert-non-empty-argument some-arg))))
  (t/is (= nil (let [some-arg "some-value"]
                 (sut/assert-non-empty-argument some-arg)))))

;; with-err-str is missing in clojure.core
(defmacro with-err-str
  "Evaluates exprs in a context in which *err* is bound to a fresh
  StringWriter.  Returns the string created by any nested printing
  calls."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*err* s#]
       ~@body
       (str s#))))

(defmacro with-err-drop
  "Evaluates exprs in a context in which *err* is bound to a fresh
  StringWriter.  Returns the original return value and drops the err
  string."
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*err* s#]
       ~@body)))

(t/deftest error-printer
  (t/is (= "An error occured:\nBla\n" (with-err-str
                                        (let [thrower (fn [_] (throw (RuntimeException. "Bla")))
                                              wrapped-fn (sut/with-error-printer thrower)]
                                          (wrapped-fn nil)))))
  (t/is (= 1 (with-err-drop
               (let [thrower (fn [_] (throw (RuntimeException. "Bla")))
                     wrapped-fn (sut/with-error-printer thrower)]
                 (wrapped-fn nil)))))
  (t/is (= nil (let [non-thrower (fn [_] nil)
                   wrapped-fn (sut/with-error-printer non-thrower)]
               (wrapped-fn nil)))))
