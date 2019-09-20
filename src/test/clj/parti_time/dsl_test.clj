(ns parti-time.dsl-test
  (:require [parti-time.dsl :as sut]
            [clojure.test :as t]
            [java-time]))

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
    (t/is (= [{:reference-date (java-time/local-date "2019-02-03")
               :time (java-time/local-time "12:15")
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
    (t/is (= [{:reference-date (java-time/local-date "2019-02-03")
               :time (java-time/local-time "12:15")
               :subject "Some Project"
               :details ["Something to do"]}
              {:time (java-time/local-time "15:15")
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
    (t/is (= [{:reference-date (java-time/local-date "2019-02-03")
               :time (java-time/local-time "12:15")
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
    (t/is (= [{:reference-date (java-time/local-date "2019-02-03")
               :time (java-time/local-time "12:15")
               :subject "Some Project"}]
             (sut/ast->entries
              [:timeline
               [:entry
                [:reference-date "2019-02-03"]
                [:hhmm-time "1215"]
                [:subject "Some Project"]]]))
          "Entry without detail")))

