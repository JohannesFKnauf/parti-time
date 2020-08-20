(ns parti-time.input.yaml-test
  (:require [yaml.core :as yaml]
            [clojure.test :as t]
            [parti-time.util.time :as time]
            [parti-time.input.yaml :as sut]))


(t/deftest yaml-input-format-test
  (t/testing "Timeslice Mapping"
    (t/is (= {:start-time (time/parse-iso-date-time "2019-07-02t11:30:00")
              :project "A Project"
              :location "Somewhere"
              :occupations ["This" "That" "Yet another" "Even More"]}
             (sut/import-yaml-timeslice
              {:starting_from "2019-07-02t11:30"
               :project "A Project"
               :location "Somewhere"
               :occupation "This,That, Yet another,    Even More"})))))

