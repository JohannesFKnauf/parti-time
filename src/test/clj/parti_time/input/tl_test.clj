(ns parti-time.input.tl-test
  (:require [parti-time.util.time :as time]
            [clojure.test :as t]
            [parti-time.input.tl :as sut]))

(t/deftest timeline-parser
  (t/testing "Entry parsing"
    (t/is (= [:timeline
              [:entry
               [:reference-date "2019-02-03"]
               [:hhmm-time "1215"]
               [:subject "Some Project"]
               [:details
                [:detail "Something to do"]]]]
             (sut/timeline-parser (str "2019-02-03\n"
                                       "1215 Some Project\n"
                                       "     Something to do")))
          "Single sane entry")
    (t/is (= [:timeline
              [:entry
               [:reference-date "2019-02-03"]
               [:hhmm-time "1215"]
               [:subject "Some Project"]
               [:details
                [:detail "Something to do"]]]
              [:entry
               [:hhmm-time "1515"]
               [:subject "Some other Project"]
               [:details
                [:detail "Something else to do"]]]]
             (sut/timeline-parser (str "2019-02-03\n"
                                       "1215 Some Project\n"
                                       "     Something to do\n"
                                       "1515 Some other Project\n"
                                       "     Something else to do")))
          "Additional entry without reference date")
    (t/is (= [:timeline
              [:entry
               [:reference-date "2019-02-03"]
               [:hhmm-time "1215"]
               [:subject "Some Project"]
               [:details
                [:detail "Something to do"]
                [:detail "Something else to do"]]]]
             (sut/timeline-parser (str "2019-02-03\n"
                                       "1215 Some Project\n"
                                       "     Something to do\n"
                                       "     Something else to do")))
          "Additional detail")
    (t/is (= [:timeline
              [:entry
               [:reference-date "2019-02-03"]
               [:hhmm-time "1215"]
               [:subject "Some Project"]]]
             (sut/timeline-parser (str "2019-02-03\n"
                                       "1215 Some Project")))
          "Entry without detail")))

(t/deftest timeline-transformation
  (t/testing "Conversion to proper hash-map"
    (t/is (= [{:reference-date (time/parse-iso-date "2019-02-03")
               :time (time/parse-iso-time "12:15:00")
               :subject "Some Project"
               :details ["Something to do"]}]
             (sut/ast->entries
              [:timeline
               [:entry
                [:reference-date "2019-02-03"]
                [:hhmm-time "1215"]
                [:subject "Some Project"]
                [:details
                 [:detail "Something to do"]]]]))
          "Single sane entry")
    (t/is (= [{:reference-date (time/parse-iso-date "2019-02-03")
               :time (time/parse-iso-time "12:15:00")
               :subject "Some Project"
               :details ["Something to do"]}
              {:time (time/parse-iso-time "15:15:00")
               :subject "Some other Project"
               :details ["Something else to do"]}]
             (sut/ast->entries
              [:timeline
               [:entry
                [:reference-date "2019-02-03"]
                [:hhmm-time "1215"]
                [:subject "Some Project"]
                [:details
                 [:detail "Something to do"]]]
               [:entry
                [:hhmm-time "1515"]
                [:subject "Some other Project"]
                [:details
                 [:detail "Something else to do"]]]]))
             "Additional entry without reference date")
    (t/is (= [{:reference-date (time/parse-iso-date "2019-02-03")
               :time (time/parse-iso-time "12:15:00")
               :subject "Some Project"
               :details ["Something to do"
                         "Something else to do"]}]
             (sut/ast->entries
              [:timeline
               [:entry
                [:reference-date "2019-02-03"]
                [:hhmm-time "1215"]
                [:subject "Some Project"]
                [:details
                 [:detail "Something to do"]
                 [:detail "Something else to do"]]]]))
          "Additional detail")
    (t/is (= [{:reference-date (time/parse-iso-date "2019-02-03")
               :time (time/parse-iso-time "12:15:00")
               :subject "Some Project"}]
             (sut/ast->entries
              [:timeline
               [:entry
                [:reference-date "2019-02-03"]
                [:hhmm-time "1215"]
                [:subject "Some Project"]]]))
          "Entry without detail")))

(t/deftest strip-comments
  (t/testing "Proper Comments"
    (t/is (= (str "some line\n"
                  "another line\n"
                  "last line")
             (sut/strip-comments
              (str "# comment in the beginning\n"
                   "some line\n"
                   "another line\n"
                   "last line")))
          "Comment in the middle")
    (t/is (= (str "some line\n"
                  "another line\n"
                  "last line")
             (sut/strip-comments
              (str "some line\n"
                   "another line\n"
                   "# a comment\n"
                   "last line")))
          "Comment in the middle"))
  (t/testing "Corner-case Comments"
    (t/is (= (str "some line\n"
                  "another line\n"
                  "last line")
             (sut/strip-comments
              (str "some line\n"
                   "another line\n"
                   "last line")))
          "No Comments")
    (t/is (= (str "some line\n"
                  "another line\n"
                   " # no comment\n"
                  "last line")
             (sut/strip-comments
              (str "some line\n"
                   "another line\n"
                   " # no comment\n"
                   "last line")))
          "Line does not begin in pound sign")))


(t/deftest entry->timeslice
  (t/testing "Valid entries"
    (t/is (= {:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupation ["Something to do"
                            "Another thing to do"]}
             (sut/entry->timeslice
              {:reference-date (time/parse-iso-date "2019-02-03")
               :time (time/parse-iso-time "12:15")
               :subject "Some Project"
               :details ["Something to do"
                         "Another thing to do"]}))
          "Complete entry")
    (t/is (= {:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupation []}
             (sut/entry->timeslice
              {:reference-date (time/parse-iso-date "2019-02-03")
               :time (time/parse-iso-time "12:15")
               :subject "Some Project"}))
          "Details omitted")))

(t/deftest fill-in-missing-reference-dates
  (t/testing "Valid entries"
    (t/is (= [{:reference-date (time/parse-iso-date "2019-02-03")
               :whatever "something"}
              {:reference-date (time/parse-iso-date "2019-02-03")
               :whatever "else"}]
             (sut/fill-in-missing-reference-dates
              [{:reference-date (time/parse-iso-date "2019-02-03")
                :whatever "something"}
               {:reference-date (time/parse-iso-date "2019-02-03")
                :whatever "else"}]))
          "Present reference dates are preserved.")
    (t/is (= [{:reference-date (time/parse-iso-date "2019-02-03")
               :whatever "something"}
              {:reference-date (time/parse-iso-date "2019-02-03")
               :whatever "else"}]
             (sut/fill-in-missing-reference-dates
              [{:reference-date (time/parse-iso-date "2019-02-03")
                :whatever "something"}
               {:whatever "else"}]))
          "Missing reference dates are filled in with the previous reference date."))
  (t/testing "Invalid entries"
    (t/is (thrown? RuntimeException
                   (sut/fill-in-missing-reference-dates
                    [{:whatever "something"}
                     {:reference-date (time/parse-iso-date "2019-02-03")
                      :whatever "else"}]))
          "First entry is missing mandatory reference date.")))

(t/deftest import-timeline
  (t/testing "Valid sample timeline"
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupation ["Something to do"
                            "Another thing to do"]}
              {:start-time (time/parse-iso-date-time "2019-02-03t15:15:00")
               :project "Some other Project"
               :occupation ["Something else to do"]}]
             (sut/import-timeline (str "2019-02-03\n"
                                       "1215 Some Project\n"
                                       "     Something to do\n"
                                       "     Another thing to do\n"
                                       "2019-02-03\n"
                                       "1515 Some other Project\n"
                                       "     Something else to do")))
          "Multiple entries, all with a reference date")
    (t/is (= [{:start-time (time/parse-iso-date-time "2019-02-03t12:15:00")
               :project "Some Project"
               :occupation ["Something to do"
                            "Another thing to do"]}
              {:start-time (time/parse-iso-date-time "2019-02-03t15:15:00")
               :project "Some other Project"
               :occupation ["Something else to do"]}]
             (sut/import-timeline (str "2019-02-03\n"
                                       "1215 Some Project\n"
                                       "     Something to do\n"
                                       "     Another thing to do\n"
                                       "1515 Some other Project\n"
                                       "     Something else to do")))
          "Multiple entries, 1 without a reference date")))
