(ns parti-time.output.api-test
    (:require [clojure.test :as t]
              [parti-time.output.api :as sut]))

(t/deftest write-timeline-multimethod
  (t/testing "Default case"
    (t/is (thrown-with-msg? RuntimeException #"parti-time does not know \(yet\) how to write file format 'abc'"
                            (sut/write-timeline "abc" "somefile.abc" []))
          "Unknown file format")))
