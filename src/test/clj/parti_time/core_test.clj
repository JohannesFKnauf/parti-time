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

(t/deftest private--test
  (t/testing "Privateness check"
    (t/is (= true (sut/is-private? {:project "Private"})))
    (t/is (= false (sut/is-private? {:project "Something Else"}))))
  (t/testing "Collapsing consecutive private entries"
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-07-02t11:30")
               :project "Private"
               :occupations []}
              {:start-time (time/parse-iso-date-time "2019-07-02t13:00")
               :project "Customer X"}
              {:start-time (time/parse-iso-date-time "2019-07-02t15:00")
               :project "Customer X"}
              {:start-time (time/parse-iso-date-time "2019-07-02t17:00")
               :project "Private"}
              {:start-time (time/parse-iso-date-time "2019-07-03t08:00")
               :project "Customer Y"}
              {:start-time (time/parse-iso-date-time "2019-07-03t09:00")
               :project "Private"
               :occupations []}
              {:start-time (time/parse-iso-date-time "2019-07-03t11:30")
               :project "Customer Z"}]
             (sut/collapse-private-details [{:start-time (time/parse-iso-date-time "2019-07-02t11:30")
                                             :project "Private"
                                             :occupations ["a" "b" "c"]}
                                            {:start-time (time/parse-iso-date-time "2019-07-02t12:00")
                                             :project "Private"
                                             :occupations ["d" "e" "f"]}
                                            {:start-time (time/parse-iso-date-time "2019-07-02t13:00")
                                             :project "Customer X"}
                                            {:start-time (time/parse-iso-date-time "2019-07-02t15:00")
                                             :project "Customer X"}
                                            {:start-time (time/parse-iso-date-time "2019-07-02t17:00")
                                             :project "Private"}
                                            {:start-time (time/parse-iso-date-time "2019-07-03t06:30")
                                             :project "Private"}
                                            {:start-time (time/parse-iso-date-time "2019-07-03t07:00")
                                             :project "Private"}
                                            {:start-time (time/parse-iso-date-time "2019-07-03t08:00")
                                             :project "Customer Y"}
                                            {:start-time (time/parse-iso-date-time "2019-07-03t09:00")
                                             :project "Private"
                                             :occupations ["g"]}
                                            {:start-time (time/parse-iso-date-time "2019-07-03t11:30")
                                             :project "Customer Z"}])))))
