(ns parti-time.input.yaml-library-test
  (:require [yaml.core :as yaml]
            [clojure.test :as t]))

(t/deftest yaml-library-test
  (t/testing "Used YAML parsing library"
    (t/testing "Lists"
      (t/is (= ["Test" "Yet another"]
               (yaml/parse-string (clojure.string/join "\n" ["- Test"
                                                             "- Yet another"])))))
    (t/testing "Empty entries"
      (t/is (= {:test nil}
               (yaml/parse-string "test: "))))))
