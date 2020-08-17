(ns parti-time.output.api-test
    (:require [clojure.test :as t]
              [parti-time.input.api :as sut]))

(t/deftest write-timeline-multimethod
  (t/testing "Error case"
    (t/is (thrown-with-msg? RuntimeException #"Filename extension .* could not be detected"
                            (sut/read-timeline "somefile"))
          "Empty filename")
    (t/is (thrown-with-msg? RuntimeException #"Filename extension .* could not be detected"
                            (sut/read-timeline "somefile"))
          "Filename without extension"))
  (t/testing "Default case"
    (t/is (thrown-with-msg? RuntimeException #"parti-time does not know .* how to read"
                            (sut/read-timeline "somefile.abc"))
          "Filename without extension")))
