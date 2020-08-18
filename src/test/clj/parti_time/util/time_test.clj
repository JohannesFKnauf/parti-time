(ns parti-time.util.time-test
  (:import [java.time LocalDateTime ZoneId])
  (:require [clojure.test :as t]
            [parti-time.util.time :as sut]))

(defn set-timezone [f]
  (with-redefs [sut/default-zoneid (ZoneId/of "Europe/Berlin")]
    (f)))

(t/use-fixtures :once set-timezone)

(t/deftest time-parsing
  (t/is (= (LocalDateTime/of 2020 8 17 20 12 13)
           (sut/utc->local-date-time "2020-08-17T18:12:13.000000Z" ))))

(t/deftest truncating
  (t/is (= (LocalDateTime/of 2020 8 17 20 12)
           (sut/truncate-to :minutes (LocalDateTime/of 2020 8 17 20 12 30))))
  (t/is (thrown-with-msg? RuntimeException #"Unsupported unit"
           (sut/truncate-to :phlubbids (LocalDateTime/of 2020 8 17 20 12 30)))))
