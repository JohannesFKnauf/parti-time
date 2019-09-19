(ns tl.instaparse-dsl-test
  (:require [tl.instaparse-dsl :as sut]
            [clojure.test :as t]))

(t/deftest work-tl-parser
  (t/testing "Date parsing"
    (t/is (= [:timeline
              [:entry
               [:isodate "2019-02-03"]]]
             (sut/work-tl "2019-02-03"))))
   (t/testing "Project parsing"
    (t/is (= [:timeline
              [:entry
               [:project
                [:hhmm-time "1215"]
                [:project-identifier "Some Project"]]]]
             (sut/work-tl "1215 book on Some Project"))))
   (t/testing "Occupations parsing"
    (t/is (= [:timeline
              [:entry
               [:occupation
                [:occupation-description "Something to do"]]]]
             (sut/work-tl "             Something to do")))))

(t/deftest timeline-transformation
  )
