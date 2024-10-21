(ns parti-time.report.days-test
  (:require [parti-time.util.time :as time]
            [clojure.test :as t]
            [parti-time.report.days :as sut]))

(def example-timeline
  "This example-timeline is a code-representation of the integration test example."
  [{:start-time (time/parse-iso-date-time "2019-08-12t05:45")
    :project "Customer X 2019-08"
    :occupations ["Some Task"]}
   {:start-time (time/parse-iso-date-time "2019-08-12t07:00")
    :project "Metamorphant"
    :occupations ["Proof-Reading Metamorphant Blog"]}
   {:start-time (time/parse-iso-date-time "2019-08-12t07:45")
    :project "Customer X 2019-08"
    :occupations ["Development of Blarz"
                  "Interesting other stuff"]}
   {:start-time (time/parse-iso-date-time "2019-08-12t11:30")
    :project "Private"
    :occupations ["Lunch Break"]}
   {:start-time (time/parse-iso-date-time "2019-08-12t12:00")
    :project "Customer X 2019-08"
    :occupations ["Architecture Whiteboard Session"
                  "Incident Blubb"
                  "Development of Blarz"]}
   {:start-time (time/parse-iso-date-time "2019-08-12t15:45")
    :project "Private"
    :occupations ["Reading Awesome Clojure"]}
   {:start-time (time/parse-iso-date-time "2019-08-12t16:15")
    :project "Customer X 2019-08"
    :occupations ["Decision draft Project Y"]}
   {:start-time (time/parse-iso-date-time "2019-08-12t17:30")
    :project "Private"
    :occupations []}
   {:start-time (time/parse-iso-date-time "2019-08-13t05:45")
    :project "Customer X 2019-08"
    :occupations ["Roadmap planning"]}
   {:start-time (time/parse-iso-date-time "2019-08-13t07:00")
    :project "Private"
    :occupations ["Reading Wonderful Clojure"]}
   {:start-time (time/parse-iso-date-time "2019-08-13t07:45")
    :project "Customer X 2019-08"
    :occupations ["Legacy Stack Analysis"
                  "Visualisation of Dependencies"]}
   {:start-time (time/parse-iso-date-time "2019-08-13t11:30")
    :project "Private"
    :occupations ["Lunch Break"]}
   {:start-time (time/parse-iso-date-time "2019-08-13t12:00")
    :project "Customer X 2019-08"
    :occupations ["Monitoring stack"
                  "Legacy Stack Analysis"
                  "Log shipping Integration"]}
   {:start-time (time/parse-iso-date-time "2019-08-13t16:15")
    :project "Metamorphant"
    :occupations ["Phone call with customer Z"]}
   {:start-time (time/parse-iso-date-time "2019-08-13t17:00")
    :project "Customer Z 2019-08"
    :occupations ["Automated DEV host setup"
                  "Build pipelines"]}
   {:start-time (time/parse-iso-date-time "2019-08-13t18:15")
    :project "Private"
    :occupations []}])


(t/deftest timebits
  (t/testing "Destructuring a time-window into timebits"
    (t/is (= (repeat 5 {:date (time/parse-iso-date "2019-08-12")
                        :project "Customer X 2019-08"})
             (sut/timebits {:start-time (time/parse-iso-date-time "2019-08-12t16:15")
                            :end-time (time/parse-iso-date-time "2019-08-12t17:30")
                            :project "Customer X 2019-08"}))
          "Regular time-window")
    (t/is (= (concat
              (repeat 26 {:date (time/parse-iso-date "2019-08-12")
                         :project "Private"})
              (repeat 23 {:date (time/parse-iso-date "2019-08-13")
                         :project "Private"}))
             (sut/timebits {:start-time (time/parse-iso-date-time "2019-08-12t17:30")
                            :end-time (time/parse-iso-date-time "2019-08-13t05:45")
                            :project "Private"}))
          "time-window crosses midnight")
    (t/is (= (concat
              (repeat 31 {:date (time/parse-iso-date "2019-08-12")
                         :project "Private"})
              (repeat 96 {:date (time/parse-iso-date "2019-08-13")
                         :project "Private"})
              (repeat 23 {:date (time/parse-iso-date "2019-08-14")
                         :project "Private"}))
             (sut/timebits {:start-time (time/parse-iso-date-time "2019-08-12t16:15")
                            :end-time (time/parse-iso-date-time "2019-08-14t05:45")
                            :project "Private"}))
          "time-window crosses multiple midnights")
    (t/is (thrown-with-msg? RuntimeException #"Error trying to create timebits"
                            (doall (sut/timebits {:start-time (time/parse-iso-date-time "2019-08-12t16:15")
                                                  :end-time (time/parse-iso-date-time "2019-08-12t17:35")
                                                  :project "Customer X 2019-08"})))
          "invalid time-window -- duration not divisible by timebit duration")))

(t/deftest days-report
  (t/testing "Reporting example days"
    (t/is (= (str
              "  Private\n"
              ". Customer X 2019-08\n"
              "_ Customer Z 2019-08\n"
              "- Metamorphant\n"
              "\n"
              "               0              3              6              9             12             15             18             21             24\n"
              "               |              |              |              |              |              |              |              |              |\n"
              "               |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |\n"
              "2019-08-12 Mon                              . .... ---. .... .... .... ..   .... .... .... ...   ... ..                                \n"
              "2019-08-13 Tue                              . ....    . .... .... .... ..   .... .... .... .... .--- ____ _\n")
             (sut/days-report example-timeline)))))
