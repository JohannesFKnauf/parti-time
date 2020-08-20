(ns parti-time.output.tl-test
    (:require [parti-time.util.time :as time]
              [clojure.test :as t]
              [parti-time.output.tl :as sut]))

(t/deftest export-timeline
  (t/testing "Printing a valid timeline"
    (t/is (= (str "2019-02-03\n"
                  "1215 Some Project\n")
             (sut/export-timeline [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
                                    :project "Some Project"
                                    :occupations []}]))
          "Single entry without occupations")
    (t/is (= (str "2019-02-03\n"
                  "1215 Some Project\n"
                  "     Something to do\n"
                  "     Another thing to do\n")
             (sut/export-timeline [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
                                    :project "Some Project"
                                    :occupations ["Something to do"
                                                  "Another thing to do"]}]))
          "Single entry")
    (t/is (= (str "2019-02-03\n"
                  "1215 Some Project\n"
                  "     Something to do\n"
                  "     Another thing to do\n"
                  "1515 Some other Project\n"
                  "     Something else to do\n")
             (sut/export-timeline [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
                                    :project "Some Project"
                                    :occupations ["Something to do"
                                                  "Another thing to do"]}
                                   {:start-time (time/parse-iso-date-time "2019-02-03t15:15:00")
                                    :project "Some other Project"
                                    :occupations ["Something else to do"]}]))
          "Multiple entries")
    (t/is (= (str "2019-02-03\n"
                  "1215 Some Project\n"
                  "     Something to do\n"
                  "     Another thing to do\n"
                  "\n"
                  "2019-02-04\n"
                  "0815 Some other Project\n"
                  "     Something else to do\n"
                  "1515 Yet another Project\n"
                  "     Yet another thing to do\n")
             (sut/export-timeline [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
                                    :project "Some Project"
                                    :occupations ["Something to do"
                                                  "Another thing to do"]}
                                   {:start-time (time/parse-iso-date-time "2019-02-04t08:15:00")
                                    :project "Some other Project"
                                    :occupations ["Something else to do"]}
                                   {:start-time (time/parse-iso-date-time "2019-02-04t15:15:00")
                                    :project "Yet another Project"
                                    :occupations ["Yet another thing to do"]}]))
          "Multiple entries, multiple days")))
