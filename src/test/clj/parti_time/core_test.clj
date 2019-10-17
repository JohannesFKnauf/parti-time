(ns parti-time.core-test
  (:require [java-time]
            [clojure.test :as t]
            [parti-time.core :as sut]))

(t/deftest time-window-test
  (t/testing "Time-Line Interpretation"
    (t/testing "Time-Window Creation"
      (t/is (= {:start-time (java-time/local-date-time "2019-07-02t11:30")
                :end-time (java-time/local-date-time "2019-07-02t12:00")
                :duration-minutes 30}
               (sut/time-window {:start-time (java-time/local-date-time "2019-07-02t11:30")}
                                {:start-time (java-time/local-date-time "2019-07-02t12:00")}))
            "Timeframe contains calculated values for end-time and duration")
      (t/is (= {:start-time (java-time/local-date-time 2019 7 2 11 30)
                :end-time (java-time/local-date-time 2019 7 2 12 00)
                :duration-minutes 30
                :foo "bar"}
               (sut/time-window {:start-time (java-time/local-date-time "2019-07-02t11:30") :foo "bar"}
                                {:start-time (java-time/local-date-time "2019-07-02t12:00")}))
            "irrelevant keys pass through untouched")
      (t/is (= {:start-time (java-time/local-date-time 2019 7 2 11 30)
                :end-time (java-time/local-date-time 2019 7 2 12 00)
                :duration-minutes 30}
               (sut/time-window {:start-time (java-time/local-date-time "2019-07-02t11:30")}
                                {:start-time (java-time/local-date-time "2019-07-02t12:00") :foo "bar"}))
            "keys from next time-slice are ignored")
      (t/is (thrown? java.lang.IllegalArgumentException
                     (sut/time-window {:start-time (java-time/local-date-time "2019-07-02t12:00")}
                                      {:start-time (java-time/local-date-time "2019-07-02t11:30")}))
            "Wrong order of time-slices causes Exception"))
    (t/testing "Time-Window streaming"
      (t/is (= [{:start-time (java-time/local-date-time 2019 7 2 11 30)
                 :end-time (java-time/local-date-time 2019 7 2 12 00)
                 :duration-minutes 30}
                {:start-time (java-time/local-date-time 2019 7 2 12 00)
                 :end-time (java-time/local-date-time 2019 7 2 15 15)
                 :duration-minutes (+ (* 3 60) 15)}]
               (sut/time-windows
                [{:start-time (java-time/local-date-time "2019-07-02t11:30")}
                 {:start-time (java-time/local-date-time "2019-07-02t12:00")}
                 {:start-time (java-time/local-date-time "2019-07-02t15:15")}]))))))
