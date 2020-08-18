(ns parti-time.input.tt-json-test
  (:require [clojure.test :as t]
            [clojure.data.json :as sut]))

                                        ; testing the expectations on the JSON parsing
(t/deftest json-parse
  (t/testing "Parsing behaviour"
    (t/is (= {:work [{:name "Some Project"
                      :start "2020-08-14T10:00:00.000001Z"
                      :end "2020-08-14T12:00:00.000001Z"}]}
             (sut/read-str (str "{"
                                "  \"work\": ["
                                "    {"
                                "      \"name\": \"Some Project\","
                                "      \"start\": \"2020-08-14T10:00:00.000001Z\","
                                "      \"end\": \"2020-08-14T12:00:00.000001Z\""
                                "    }"
                                "  ]"
                                "}")
                           :key-fn keyword)))))
