(ns parti-time.input.yaml-test
  (:require [yaml.core :as yaml]
            [clojure.test :as t]
            [parti-time.input.yaml :as sut]))


(t/deftest yaml-input-format-test
  (t/testing "Timestamp Parsing"
    (t/is (= (java-time/local-date-time 2019 7 2 11 30)
             (sut/parse-iso-time "2019-07-02t11:30")))
    (t/is (thrown? java.lang.Exception
                   (sut/parse-iso-time nil))))
  (t/testing "Timeslice Mapping"
    (t/is (= {:start-time (java-time/local-date-time 2019 7 2 11 30)
              :project "A Project"
              :location "Somewhere"
              :occupation ["This" "That" "Yet another" "Even More"]}
             (sut/import-yaml-timeslice
              {:starting_from "2019-07-02t11:30"
               :project "A Project"
               :location "Somewhere"
               :occupation "This,That, Yet another,    Even More"})))))

