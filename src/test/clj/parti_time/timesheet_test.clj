(ns parti-time.timesheet-test
  (:require [parti-time.util.time :as time]
            [clojure.test :as t]
            [parti-time.timesheet :as sut]))

(t/deftest timesheet-test
  (t/testing "Row creation"
    (t/is (= ["2019-07-02" "11:30" "12:00" "" "Do this, Do that" "Customer X"]
             (sut/day-row {:start-time (time/parse-iso-date-time "2019-07-02t11:30")
                           :end-time (time/parse-iso-date-time "2019-07-02t12:00")
                           :occupations ["Do this"
                                         "Do that"]
                           :project "Customer X"}))
          "Simple row")
    (t/is (= ["2019-07-02" "22:30" "24:00" "" "Do this, Do that" "Customer X"]
             (sut/day-row {:start-time (time/parse-iso-date-time "2019-07-02t22:30")
                           :end-time (time/parse-iso-date-time "2019-07-03t00:00")
                           :occupations ["Do this"
                                         "Do that"]
                           :project "Customer X"}))
          "Entry ending at midnight")
    (t/is (= ["2019-07-02" "00:00" "11:30" "" "Do this, Do that" "Customer X"]
             (sut/day-row {:start-time (time/parse-iso-date-time "2019-07-02t00:00")
                           :end-time (time/parse-iso-date-time "2019-07-02t11:30")
                           :occupations ["Do this"
                                         "Do that"]
                           :project "Customer X"}))
          "Entry starting at midnight"))
  (t/testing "Splitting into days"
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-07-02t11:30")
               :end-time (time/parse-iso-date-time "2019-07-02t12:00")
               :occupations ["Do this"
                             "Do that"]
               :project "Customer X"}]
             (vec
              (sut/split-into-days {:start-time (time/parse-iso-date-time "2019-07-02t11:30")
                                    :end-time (time/parse-iso-date-time "2019-07-02t12:00")
                                    :occupations ["Do this"
                                                  "Do that"]
                                    :project "Customer X"})))
          "Simple time-window")
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-07-01t22:30")
               :end-time (time/parse-iso-date-time "2019-07-02t00:00")
               :occupations ["Do this"
                             "Do that"]
               :project "Customer X"}]
             (vec
              (sut/split-into-days {:start-time (time/parse-iso-date-time "2019-07-01t22:30")
                                    :end-time (time/parse-iso-date-time "2019-07-02t00:00")
                                    :occupations ["Do this"
                                                  "Do that"]
                                    :project "Customer X"})))
             "Timeframe ends at midnight")
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-07-02t23:30")
               :end-time (time/parse-iso-date-time "2019-07-03t00:00")
               :occupations ["Do this"
                             "Do that"]
               :project "Customer X"}
              {:start-time (time/parse-iso-date-time "2019-07-03t00:00")
               :end-time (time/parse-iso-date-time "2019-07-04t00:00")
               :occupations ["Do this"
                             "Do that"]
               :project "Customer X"}
              {:start-time (time/parse-iso-date-time "2019-07-04t00:00")
               :end-time (time/parse-iso-date-time "2019-07-04t02:00")
               :occupations ["Do this"
                             "Do that"]
               :project "Customer X"}]
             (vec
              (sut/split-into-days {:start-time (time/parse-iso-date-time "2019-07-02t23:30")
                                    :end-time (time/parse-iso-date-time "2019-07-04t02:00")
                                    :occupations ["Do this"
                                                  "Do that"]
                                    :project "Customer X"})))
          "A multi-day-booking splits at midnights")))
