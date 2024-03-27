(ns parti-time.core-test
  (:require [parti-time.util.time :as time]
            [clojure.test :as t]
            [parti-time.core :as sut]))

(t/deftest time-window-test
  (t/testing "Time-Line Interpretation"
    (t/testing "Time-Window Creation"
      (t/is (= {:start-time (time/parse-iso-date-time "2019-07-02t11:30")
                :end-time (time/parse-iso-date-time "2019-07-02t12:00")
                :duration-minutes 30}
               (sut/time-window {:start-time (time/parse-iso-date-time "2019-07-02t11:30")}
                                {:start-time (time/parse-iso-date-time "2019-07-02t12:00")}))
            "Timeframe contains calculated values for end-time and duration")
      (t/is (= {:start-time (time/parse-iso-date-time "2019-07-02t11:30:00")
                :end-time (time/parse-iso-date-time "2019-07-02t12:00:00")
                :duration-minutes 30
                :foo "bar"}
               (sut/time-window {:start-time (time/parse-iso-date-time "2019-07-02t11:30") :foo "bar"}
                                {:start-time (time/parse-iso-date-time "2019-07-02t12:00")}))
            "irrelevant keys pass through untouched")
      (t/is (= {:start-time (time/parse-iso-date-time "2019-07-02t11:30:00")
                :end-time (time/parse-iso-date-time "2019-07-02t12:00:00")
                :duration-minutes 30}
               (sut/time-window {:start-time (time/parse-iso-date-time "2019-07-02t11:30")}
                                {:start-time (time/parse-iso-date-time "2019-07-02t12:00") :foo "bar"}))
            "keys from next time-slice are ignored")
      (t/is (thrown? java.lang.IllegalArgumentException
                     (sut/time-window {:start-time (time/parse-iso-date-time "2019-07-02t12:00")}
                                      {:start-time (time/parse-iso-date-time "2019-07-02t11:30")}))
            "Wrong order of time-slices causes Exception"))
    (t/testing "Time-Window streaming"
      (t/is (= [{:start-time (time/parse-iso-date-time "2019-07-02t11:30:00")
                 :end-time (time/parse-iso-date-time "2019-07-02t12:00:00")
                 :duration-minutes 30}
                {:start-time (time/parse-iso-date-time "2019-07-02t12:00:00")
                 :end-time (time/parse-iso-date-time "2019-07-02t15:15:00")
                 :duration-minutes (+ (* 3 60) 15)}]
               (sut/time-windows
                [{:start-time (time/parse-iso-date-time "2019-07-02t11:30")}
                 {:start-time (time/parse-iso-date-time "2019-07-02t12:00")}
                 {:start-time (time/parse-iso-date-time "2019-07-02t15:15")}]))))))

(t/deftest gen-time-slices-test
  (t/testing "Adjacent time-windows"
    (t/is (= [{:start-time (time/parse-iso-date-time "2024-03-03t11:00")}]
             (sut/gen-time-slices {:start-time (time/parse-iso-date-time "2024-03-03t11:00")
                                   :end-time (time/parse-iso-date-time "2024-03-03t11:30")}
                                  {:start-time (time/parse-iso-date-time "2024-03-03t11:30")
                                   :end-time (time/parse-iso-date-time "2024-03-03t12:30")}))))
  (t/testing "Time-windows with gap"
    (t/is (= [{:start-time (time/parse-iso-date-time "2024-03-03t11:00")}
              {:start-time (time/parse-iso-date-time "2024-03-03t11:30")
               :project "Private"}]
             (sut/gen-time-slices {:start-time (time/parse-iso-date-time "2024-03-03t11:00")
                                   :end-time (time/parse-iso-date-time "2024-03-03t11:30")}
                                  {:start-time (time/parse-iso-date-time "2024-03-03t12:30")
                                   :end-time (time/parse-iso-date-time "2024-03-03t13:30")}))))
  (t/testing "Overlap in time-windows"
    (t/is (thrown? java.lang.IllegalArgumentException
                   (sut/gen-time-slices {:start-time (time/parse-iso-date-time "2024-03-03t11:00")
                                         :end-time (time/parse-iso-date-time "2024-03-03t12:30")}
                                        {:start-time (time/parse-iso-date-time "2024-03-03t11:30")
                                         :end-time (time/parse-iso-date-time "2024-03-03t13:30")}))))
  (t/testing "Invalid Time-window: End time before start time"
    (t/is (thrown? java.lang.IllegalArgumentException
                   (sut/gen-time-slices {:start-time (time/parse-iso-date-time "2024-03-03t11:30")
                                         :end-time (time/parse-iso-date-time "2024-03-03t11:00")}
                                        {:start-time (time/parse-iso-date-time "2024-03-03t12:30")
                                         :end-time (time/parse-iso-date-time "2024-03-03t13:30")}))))
  (t/testing "Last time-window"
    (t/is (= [{:start-time (time/parse-iso-date-time "2024-03-03t11:00")}
              {:start-time (time/parse-iso-date-time "2024-03-03t11:30")
               :project "Private"}]
             (sut/gen-time-slices {:start-time (time/parse-iso-date-time "2024-03-03t11:00")
                                   :end-time (time/parse-iso-date-time "2024-03-03t11:30")}
                                  nil)))))

(t/deftest time-windows->time-line-test
  (t/testing "Adjacent time-windows"
    (t/is (= [{:start-time (time/parse-iso-date-time "2024-03-03t11:00")}
              {:start-time (time/parse-iso-date-time "2024-03-03t11:30")}
              {:start-time (time/parse-iso-date-time "2024-03-03t12:30")
               :project "Private"}]
             (sut/time-windows->time-line [{:start-time (time/parse-iso-date-time "2024-03-03t11:00")
                                            :end-time (time/parse-iso-date-time "2024-03-03t11:30")}
                                           {:start-time (time/parse-iso-date-time "2024-03-03t11:30")
                                            :end-time (time/parse-iso-date-time "2024-03-03t12:30")}]))))
  (t/testing "Time-windows with gap"
    (t/is (= [{:start-time (time/parse-iso-date-time "2024-03-03t11:00")}
              {:start-time (time/parse-iso-date-time "2024-03-03t11:30")
               :project "Private"}
              {:start-time (time/parse-iso-date-time "2024-03-03t12:30")}
              {:start-time (time/parse-iso-date-time "2024-03-03t13:30")
               :project "Private"}]
             (sut/time-windows->time-line [{:start-time (time/parse-iso-date-time "2024-03-03t11:00")
                                           :end-time (time/parse-iso-date-time "2024-03-03t11:30")}
                                          {:start-time (time/parse-iso-date-time "2024-03-03t12:30")
                                           :end-time (time/parse-iso-date-time "2024-03-03t13:30")}]))))
  (t/testing "Overlap in time-windows"
    (t/is (thrown? java.lang.IllegalArgumentException
                   (sut/time-windows->time-line [{:start-time (time/parse-iso-date-time "2024-03-03t11:00")
                                                  :end-time (time/parse-iso-date-time "2024-03-03t12:30")}
                                                 {:start-time (time/parse-iso-date-time "2024-03-03t11:30")
                                                  :end-time (time/parse-iso-date-time "2024-03-03t13:30")}]))))
  (t/testing "Invalid Time-window: End time before start time"
    (t/is (thrown? java.lang.IllegalArgumentException
                   (sut/time-windows->time-line [{:start-time (time/parse-iso-date-time "2024-03-03t11:30")
                                                 :end-time (time/parse-iso-date-time "2024-03-03t11:00")}
                                                {:start-time (time/parse-iso-date-time "2024-03-03t12:30")
                                                 :end-time (time/parse-iso-date-time "2024-03-03t13:30")}]))))
  (t/testing "Last time-window"
    (t/is (= [{:start-time (time/parse-iso-date-time "2024-03-03t11:00")}
              {:start-time (time/parse-iso-date-time "2024-03-03t11:30")
               :project "Private"}]
             (sut/time-windows->time-line [{:start-time (time/parse-iso-date-time "2024-03-03t11:00")
                                           :end-time (time/parse-iso-date-time "2024-03-03t11:30")}])))))
