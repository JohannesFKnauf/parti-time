(defproject parti-time "0.1.0-SNAPSHOT"
  :description "parti-time is a tool for partitioning timelines."
  :url "https://github.com/JohannesFKnauf/parti-time"
  :min-lein-version "2.8.1"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.flatland/ordered "1.5.7"]
                 [io.forward/yaml "1.0.9"]
                 [clojure.java-time "0.3.2"]
                 [clojure-csv "2.0.2"]
                 [instaparse "1.4.10"]
                 [medley "1.2.0"]]
  :source-paths ["src/main/clj"]
  :test-paths ["src/test/clj" "src/itest/clj"]
  :test-selectors {:default (complement :integration)
                   :integration :integration
                   :all (constantly true)}
  :resource-paths ["src/main/resources"]
  :dev {:resource-paths ["src/test/resources" "src/itest/resources"]}
  :aliases {"itest" ["test" ":integration"]}
  :clean-targets ["target"]
  :main parti-time.cli)
