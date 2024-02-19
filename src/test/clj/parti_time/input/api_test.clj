(ns parti-time.input.api-test
    (:require [clojure.test :as t]
              [parti-time.input.api :as sut]))

(t/deftest read-timeline-multimethod
  (t/testing "Default case"
    (t/is (thrown-with-msg? RuntimeException #"parti-time does not know \(yet\) how to read file format 'abc'"
                            (sut/read-timeline "abc" "somefile.abc"))
          "Unknown file format")))
