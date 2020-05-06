(defproject parti-time "0.1.0-SNAPSHOT"
  :description "parti-time is a tool for partitioning timelines."
  :url "https://github.com/JohannesFKnauf/parti-time"
  :min-lein-version "2.8.1"
  :global-vars {*warn-on-reflection* true}
  :dependencies [[org.clojure/clojure "1.10.2-alpha1"]
                 [org.flatland/ordered "1.5.7"]
                 [io.forward/yaml "1.0.9"]
                 [clojure-csv "2.0.2"]
                 [instaparse "1.4.10"]
                 [tick "0.4.23-alpha"]
                 [medley "1.2.0"]]
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
                 :graal-bin "/home/vagrant/graalvm-ce-java11-19.3.1/"
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
