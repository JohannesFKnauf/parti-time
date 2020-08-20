(ns parti-time.input.tl-test
  (:require [parti-time.util.time :as time]
            [clojure.test :as t]
            [parti-time.input.tl :as sut]))

(t/deftest timeline-parser
  (t/testing "Entry parsing"
    (t/is (= [:timeline
              [:day
               [:reference-date "2019-02-03"]
               [:entries
                [:entry
                 [:hhmm-time "1215"]
                 [:project "Some Project"]
                 [:activities
                  [:activity "Something to do"]]]]]]
             (sut/timeline-parser (str "2019-02-03\n"
                                       "1215 Some Project\n"
                                       "     Something to do\n")))
          "Single sane entry")
    (t/is (= [:timeline
              [:day
               [:reference-date "2019-02-03"]
               [:entries
                [:entry
                 [:hhmm-time "1215"]
                 [:project "Some Project"]
                 [:activities
                  [:activity "Something to do"]]]
                [:entry
                 [:hhmm-time "1515"]
                 [:project "Some other Project"]
                 [:activities
                  [:activity "Something else to do"]]]]]]
             (sut/timeline-parser (str "2019-02-03\n"
                                       "1215 Some Project\n"
                                       "     Something to do\n"
                                       "1515 Some other Project\n"
                                       "     Something else to do\n")))
          "Multiple entries for a single day")
    (t/is (= [:timeline
              [:day
               [:reference-date "2019-02-03"]
               [:entries
                [:entry
                 [:hhmm-time "1215"]
                 [:project "Some Project"]
                 [:activities
                  [:activity "Something to do"]
                  [:activity "Something else to do"]]]]]]
             (sut/timeline-parser (str "2019-02-03\n"
                                       "1215 Some Project\n"
                                       "     Something to do\n"
                                       "     Something else to do\n")))
          "Multiple activities")
    (t/is (= [:timeline
              [:day
               [:reference-date "2019-02-03"]
               [:entries
                [:entry
                 [:hhmm-time "1215"]
                 [:project "Some Project"]]]]]
             (sut/timeline-parser (str "2019-02-03\n"
                                       "1215 Some Project\n")))
          "Entry without activities")))


(t/deftest timeline-transformation
  (t/testing "Conversion to proper hash-map"
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupations ["Something to do"]}]
             (sut/ast->entries
              [:timeline
               [:day
                [:reference-date "2019-02-03"]
                [:entries
                 [:entry
                  [:hhmm-time "1215"]
                  [:project "Some Project"]
                  [:activities
                   [:activity "Something to do"]]]]]]))
          "Single sane entry")
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupations ["Something to do"]}
              {:start-time (time/parse-iso-date-time "2019-02-03t15:15:00")
               :project "Some other Project"
               :occupations ["Something else to do"]}]
             (sut/ast->entries
              [:timeline
               [:day
                [:reference-date "2019-02-03"]
                [:entries
                 [:entry
                  [:hhmm-time "1215"]
                  [:project "Some Project"]
                  [:activities
                   [:activity "Something to do"]]]
                 [:entry
                  [:hhmm-time "1515"]
                  [:project "Some other Project"]
                  [:activities
                   [:activity "Something else to do"]]]]]]))
             "Additional entry without its own reference date")
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupations ["Something to do"
                             "Something else to do"]}]
             (sut/ast->entries
              [:timeline
               [:day
                [:reference-date "2019-02-03"]
                [:entries
                 [:entry
                  [:hhmm-time "1215"]
                  [:project "Some Project"]
                  [:activities
                   [:activity "Something to do"]
                   [:activity "Something else to do"]]]]]]))
          "Additional activities")
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupations []}]
             (sut/ast->entries
              [:timeline
               [:day
                [:reference-date "2019-02-03"]
                [:entries
                 [:entry
                  [:hhmm-time "1215"]
                  [:project "Some Project"]]]]]))
          "Entry without activity")
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupations ["Something to do"
                             "Something else to do"]}
              {:start-time (time/parse-iso-date-time "2019-02-04t12:15:00")
               :project "Some Project"
               :occupations ["Something to do"
                             "Something else to do"]}]
             (sut/ast->entries
              [:timeline
               [:day
                [:reference-date "2019-02-03"]
                [:entries
                 [:entry
                  [:hhmm-time "1215"]
                  [:project "Some Project"]
                  [:activities
                   [:activity "Something to do"]
                   [:activity "Something else to do"]]]]]
               [:day
                [:reference-date "2019-02-04"]
                [:entries
                 [:entry
                  [:hhmm-time "1215"]
                  [:project "Some Project"]
                  [:activities
                   [:activity "Something to do"]
                   [:activity "Something else to do"]]]]]]))
          "Multiple days")))

(t/deftest entry->timeslice
  (t/testing "Valid entries"
    (t/is (= {:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupations ["Something to do"
                             "Another thing to do"]}
             (sut/entry->timeslice
              (time/parse-iso-date "2019-02-03")
              {:time (time/parse-iso-time "12:15")
               :project "Some Project"
               :activities ["Something to do"
                            "Another thing to do"]}))
          "Complete entry")
    (t/is (= {:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupations []}
             (sut/entry->timeslice
              (time/parse-iso-date "2019-02-03")
              {:time (time/parse-iso-time "12:15")
               :project "Some Project"}))
          "Details omitted")))

(t/deftest import-timeline
  (t/testing "Valid sample timeline"
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupations ["Something to do"
                             "Another thing to do"]}
              {:start-time (time/parse-iso-date-time "2019-02-03t15:15:00")
               :project "Some other Project"
               :occupations ["Something else to do"]}]
             (sut/import-timeline (str "2019-02-03\n"
                                       "1215 Some Project\n"
                                       "     Something to do\n"
                                       "     Another thing to do\n"
                                       "\n"
                                       "2019-02-03\n"
                                       "1515 Some other Project\n"
                                       "     Something else to do\n")))
          "Multiple entries, all with a reference date")
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupations ["Something to do"
                             "Another thing to do"]}
              {:start-time (time/parse-iso-date-time "2019-02-03t15:15:00")
               :project "Some other Project"
               :occupations ["Something else to do"]}]
             (sut/import-timeline (str "2019-02-03\n"
                                       "1215 Some Project\n"
                                       "     Something to do\n"
                                       "     Another thing to do\n"
                                       "1515 Some other Project\n"
                                       "     Something else to do\n")))
          "Multiple entries, 1 without a reference date")))

(t/deftest bogus-timeline
  (t/testing "Syntactical errors"
    (t/is (thrown-with-msg? RuntimeException #"(?s)Parse error at line 1, column 1:\na2019-02-03.*Expected:\n#\"\\p\{Digit\}"
                            (sut/import-timeline  (str "a2019-02-03\n"
                                                       "1215 Some Project\n")))
          "Wrong date")
    (t/is (thrown-with-msg? RuntimeException #"(?s)Parse error at line 2, column 18:.*Expected:\n#\"\\r\?\\n\""
                            (sut/import-timeline  (str "2019-02-03\n"
                                                       "1215 Some Project")))
          "Missing mandatory trailing newline")
    (t/is (thrown-with-msg? RuntimeException #"(?s)Parse error at line 4, column 1:.*Expected:\n#\"\\p\{Digit\}"
                            (sut/import-timeline  (str "2019-02-03\n"
                                                       "1215 Some Project\n"
                                                       "\n")))
          "Excess trailing newline")
    (t/is (thrown-with-msg? RuntimeException #"(?s)Parse error at line 6, column 1:.*Expected:\n#\"\\p\{Digit\}"
                            (sut/import-timeline  (str "2019-02-03\n"
                                                       "1215 Some Project\n"
                                                       "     Something to do\n"
                                                       "     Another thing to do\n"
                                                       "\n"
                                                       "\n"
                                                       "2019-02-03\n"
                                                       "1515 Some other Project\n"
                                                       "     Something else to do\n")))
          "More than a single emtpy line between days")
    (t/is (thrown-with-msg? RuntimeException #"(?s)Parse error at line 5, column 5:.*Expected:\n \n"
                            (sut/import-timeline  (str "2019-02-03\n"
                                                       "1215 Some Project\n"
                                                       "     Something to do\n"
                                                       "     Another thing to do\n"
                                                       "2019-02-03\n"
                                                       "1515 Some other Project\n"
                                                       "     Something else to do\n")))
          "No emtpy line between days")
    (t/is (thrown-with-msg? RuntimeException #"(?s)Parse error at line 1, column 1:.*Expected:\n#\"\\p\{Digit\}"
                            (sut/import-timeline  (str "\n"
                                                       "2019-02-03\n"
                                                       "1215 Some Project\n")))
          "Leading newline is invalid")))
