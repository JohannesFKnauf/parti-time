(ns tl.instaparse-dsl-test
  (:require [tl.instaparse-dsl :as sut]
            [clojure.test :as t]))

(t/deftest work-tl-parser
  (t/testing "Entry parsing"
    (t/is (= [:timeline
              [:entry
               [:isodate "2019-02-03"]
               [:start-on-project
                [:hhmm-time "1215"]
                [:project-identifier "Some Project"]]
               [:occupation [:occupation-description "Something to do"]]]]
             (sut/work-tl (str "2019-02-03\n"
                               "1215 book on Some Project\n"
                               "             Something to do"))))))

(t/deftest timeline-transformation
  )
