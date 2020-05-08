(ns parti-time.cli-integration-test
  (:require [clojure.test :as t]
            [parti-time.cli :as sut]))

(t/deftest ^:integration cli-integration-test
  (t/is (= true true)))
