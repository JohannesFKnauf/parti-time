(ns tl.instaparse-dsl-test
  (:require [tl.instaparse-dsl :as sut]
            [clojure.test :as t]))

(t/deftest timeline-parser
  (t/testing "Entry parsing"
    (t/is (= [:timeline
              [:entry
               [:reference-date "2019-02-03"]
               [:time-partition-start
                [:hhmm-time "1215"]
                [:subject "Some Project"]]
               [:detail "Something to do"]]]
             (sut/timeline-parser (str "2019-02-03\n"
                                       "1215 Some Project\n"
                                       "     Something to do"))))))

(t/deftest timeline-transformation
  )
