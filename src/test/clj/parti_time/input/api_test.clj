(ns parti-time.input.api-test
    (:require [clojure.test :as t]
              [parti-time.input.api :as sut]))

(t/deftest read-timeline-multimethod
  (t/testing "Default case"
    (t/is (thrown-with-msg? RuntimeException #"parti-time does not know .* how to read"
                            (sut/read-timeline "somefile.abc"))
          "Filename with unknown extension")))
