(ns parti-time.input.tt-test
  (:require [parti-time.util.time :as time]
            [clojure.test :as t]
            [parti-time.input.tt :as sut]))

; we need to pin down the timezone for reproducible tests
(defn set-timezone [f]
  (with-redefs [time/default-zoneid (time/zoneid "Europe/Berlin")]
    (f)))

(t/use-fixtures :once set-timezone)


(t/deftest tt-import-timeline
  (t/testing "JSON validation"
    (t/is (thrown-with-msg? Exception #"JSON error \(unexpected character\)"
                            (sut/import-timeline (str "{ ... }")))
          "Invalid JSON throws exception")))

(t/deftest interpret-entry
  (t/is (= {:name "Some Project"
            :start (time/parse-iso-date-time "2020-08-17T12:00")
            :end (time/parse-iso-date-time "2020-08-17T14:00")}
           (sut/interpret-entry {:name "Some Project"
                                 :start "2020-08-17T10:00:00.000000Z"
                                 :end "2020-08-17T12:00:00.000000Z"}))
        "times are interpreted as local times in valid entry")
  (t/is (= {:name "Some Project"
            :start (time/parse-iso-date-time "2020-08-17T12:12")
            :end (time/parse-iso-date-time "2020-08-17T14:00")}
           (sut/interpret-entry {:name "Some Project"
                                 :start "2020-08-17T10:12:34.000000Z"
                                 :end "2020-08-17T12:00:00.123456Z"}))
        "seconds and subsecond precision timestamps are truncated to minutes")
  (t/is (thrown-with-msg? RuntimeException #"Mandatory field ':start' is missing"
                 (sut/interpret-entry {:name "Some Project"
                                       :end "2020-08-17T10:00:00.000000Z"}))
        "missing start time yields Exception")
  (t/is (thrown-with-msg? RuntimeException #"Mandatory field ':end' is missing"
                 (sut/interpret-entry {:name "Some Project"
                                       :start "2020-08-17T10:00:00.000000Z"}))
        "missing end time yields Exception")
  (t/is (thrown-with-msg? RuntimeException #"Mandatory field ':name' is missing"
                 (sut/interpret-entry {:start "2020-08-17T10:00:00.000000Z"
                                       :end "2020-08-17T12:00:00.000000Z"}))
        "missing name yields Exception")
  (t/is (thrown? java.time.format.DateTimeParseException
                 (sut/interpret-entry {:name "Some Project"
                                       :start "2020-08-17 12:00:00"
                                       :end "2020-08-17T10:00:00.000000Z"}))
        "unparsable start time yields Exception")
  (t/is (thrown? java.time.format.DateTimeParseException
                 (sut/interpret-entry {:name "Some Project"
                                       :start "2020-08-17T12:00:00.000000Z"
                                       :end "2020-08-17 10:00:00"}))
        "unparsable end time yields Exception")
  (t/is (thrown-with-msg? RuntimeException #"End time .* is before start time"
                 (sut/interpret-entry {:name "Some Project"
                                       :start "2020-08-17T12:00:00.000000Z"
                                       :end "2020-08-17T10:00:00.000000Z"}))
        "end time before start time yields Exception")
  (t/is (not (contains? (sut/interpret-entry {:name "Some Project"
                                              :start "2020-08-17T10:00:00.000000Z"
                                              :end "2020-08-17T12:00:00.000000Z"
                                              :tags ["a-tag"
                                                     "another-tag"]})
                        :tags))
        "tags are ignored and dropped") ; tags are a dead and unused feature in tt, according to its author dribnif
  (t/is (= ["Fixed such and such"
            "Developed this and that"]
           (:notes (sut/interpret-entry {:name "Some Project"
                                         :start "2020-08-17T10:00:00.000000Z"
                                         :end "2020-08-17T12:00:00.000000Z"
                                         :notes ["Fixed such and such"
                                                 "Developed this and that"]})))
        "notes are passed through"))


(t/deftest emit-timeslices
  (t/is (= [{:start-time (time/parse-iso-date-time "2020-08-12T10:00:00")
             :project "Some project"
             :occupations []}]
           (sut/emit-timeslices {:start (time/parse-iso-date-time "2020-08-12T10:00:00")
                                 :end (time/parse-iso-date-time "2020-08-12T12:00:00")
                                 :name "Some project"
                                 :notes []}
                                {:start (time/parse-iso-date-time "2020-08-12T12:00:00")
                                 :end (time/parse-iso-date-time "2020-08-12T14:00:00")
                                 :name "Irrelevant"
                                 :notes []}))
        "A single timeslice is emitted for adjoined entries")
  (t/is (= [{:start-time (time/parse-iso-date-time "2020-08-12T10:00:00")
             :project "Some project"
             :occupations []}
            {:start-time (time/parse-iso-date-time "2020-08-12T12:00:00")
             :project "Private"
             :occupations []}]
           (sut/emit-timeslices {:start (time/parse-iso-date-time "2020-08-12T10:00:00")
                                 :end (time/parse-iso-date-time "2020-08-12T12:00:00")
                                 :name "Some project"
                                 :notes []}
                                {:start (time/parse-iso-date-time "2020-08-12T13:00:00")
                                 :end (time/parse-iso-date-time "2020-08-12T15:00:00")
                                 :name "Irrelevant"
                                 :notes []}))
        "A private filler timeslice is emitted additionally for entries with a gap in between"))

(t/deftest worklog-to-timeline
  (t/testing "Empty worklog"
    (t/is (= []
             (sut/tt-worklog->timeline {}))
          "work element missing yields empty list")
    (t/is (= []
             (sut/tt-worklog->timeline {:work []}))
          "work element empty list yields empty list"))
  (t/testing "Valid worklog"
    (t/is (= [{:start-time (time/parse-iso-date-time "2020-08-17T10:00:00")
               :project "Some Project"
               :occupations ["This"
                             "That"]}
              {:start-time (time/parse-iso-date-time "2020-08-17T12:00:00")
               :project "Private"
               :occupations []}
              {:start-time (time/parse-iso-date-time "2020-08-17T13:00:00")
               :project "Some other Project"
               :occupations []}
              {:start-time (time/parse-iso-date-time "2020-08-17T15:00:00")
               :project "Private"
               :occupations []}]
             (sut/tt-worklog->timeline {:work [{:start  "2020-08-17T08:00:00.000000Z"
                                                :end "2020-08-17T10:00:00.000000Z"
                                                :name "Some Project"
                                                :notes ["This"
                                                        "That"]}
                                               {:start "2020-08-17T11:00:00.000000Z"
                                                :end "2020-08-17T13:00:00.000000Z"
                                                :name "Some other Project"
                                                :notes []}]}))
          "For a valid worklog, gaps are filled with Private entries")
    (t/is (= [{:start-time (time/parse-iso-date-time "2020-08-17T10:00:00")
               :project "Some Project"
               :occupations ["This"
                             "That"]}
              {:start-time (time/parse-iso-date-time "2020-08-17T12:00:00")
               :project "Private"
               :occupations []}
              {:start-time (time/parse-iso-date-time "2020-08-17T13:00:00")
               :project "Some other Project"
               :occupations []}
              {:start-time (time/parse-iso-date-time "2020-08-17T15:00:00")
               :project "Private"
               :occupations []}]
             (sut/tt-worklog->timeline {:work [{:start "2020-08-17T11:00:00.000000Z"
                                                :end "2020-08-17T13:00:00.000000Z"
                                                :name "Some other Project"
                                                :notes []}
                                               {:start  "2020-08-17T08:00:00.000000Z"
                                                :end "2020-08-17T10:00:00.000000Z"
                                                :name "Some Project"
                                                :notes ["This"
                                                        "That"]}]}))
          "Disordered worklogs are perfectly permissible and yield the same result as ordered worklogs")
    (t/is (= [{:start-time (time/parse-iso-date-time "2020-08-17T10:00:00")
               :project "Some Project"
               :occupations ["This"
                             "That"]}
              {:start-time (time/parse-iso-date-time "2020-08-17T13:00:00")
               :project "Some other Project"
               :occupations []}
              {:start-time (time/parse-iso-date-time "2020-08-17T15:00:00")
               :project "Private"
               :occupations []}]
             (sut/tt-worklog->timeline {:work [{:start  "2020-08-17T08:00:00.000000Z"
                                                :end "2020-08-17T11:00:00.000000Z"
                                                :name "Some Project"
                                                :notes ["This"
                                                        "That"]}
                                               {:start "2020-08-17T11:00:00.000000Z"
                                                :end "2020-08-17T13:00:00.000000Z"
                                                :name "Some other Project"
                                                :notes []}]}))
          "For adjoined entries, no gap filler is inserted"))
  (t/testing "Invalid worklog"
    (t/is (thrown-with-msg? RuntimeException #"Mandatory field ':end' is missing"
                            (sut/tt-worklog->timeline {:work [{:start  "2020-08-17T08:00:00.000000Z"
                                                               :name "Some Project"}]}))
          "Missing end tag throws Exception -- even in end position")
    (t/is (thrown-with-msg? RuntimeException #"Overlapping entries detected"
                            (sut/tt-worklog->timeline {:work [{:start  "2020-08-17T08:00:00.000000Z"
                                                               :end "2020-08-17T11:00:00.000000Z"
                                                               :name "Some Project"
                                                               :notes ["This"
                                                                       "That"]}
                                                              {:start "2020-08-17T10:00:00.000000Z"
                                                               :end "2020-08-17T13:00:00.000000Z"
                                                               :name "Some other Project"
                                                               :notes []}]}))
          "Overlap between entries throws Exception")))




