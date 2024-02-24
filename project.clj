(defproject parti-time "1.2.1-SNAPSHOT"
  :description "parti-time is a tool for partitioning timelines."
  :url "https://github.com/JohannesFKnauf/parti-time"
  :min-lein-version "2.11.1"
  :global-vars {*warn-on-reflection* true}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.flatland/ordered "1.15.11"]
                 [io.forward/yaml "1.0.11"]
                 [cli-matic "0.5.4"]
                 [clojure-csv "2.0.2"]
                 [instaparse "1.4.12"]
                 [medley "1.4.0"]]
  :plugins [[io.taylorwood/lein-native-image "0.3.1"]]

  :source-paths ["src/main/clj"]
  :test-paths ["src/test/clj" "src/itest/clj"]
  :test-selectors {:default (complement :integration)
                   :integration :integration
                   :all (constantly true)}
  :resource-paths ["src/main/resources"]
  :dev {:resource-paths ["src/test/resources" "src/itest/resources"]}
  :aliases {"itest" ["test" ":integration"]}
  :clean-targets ["target"]

  :native-image {:name "parti-time"
                 :opts ["--verbose"
                        "--initialize-at-build-time"
                        "--report-unsupported-elements-at-runtime"
                        "-H:+ReportExceptionStackTraces"
                        "--no-fallback"
                        "--no-server"]}
  :profiles {:test
             {:native-image {:opts ["--report-unsupported-elements-at-runtime"
                                    "--initialize-at-build-time"
                                    "--verbose"]}}
             
             :uberjar
             {:aot :all
              :native-image {:jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}}

  :main parti-time.cli)
