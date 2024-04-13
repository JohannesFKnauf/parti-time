(defproject parti-time "lein-git-inject/version"
  :description "parti-time is a tool for partitioning timelines."
  :url "https://github.com/JohannesFKnauf/parti-time"
  :min-lein-version "2.11.1"
  :global-vars {*warn-on-reflection* true}
  :dependencies [[cheshire "5.13.0"]
                 [cli-matic "0.5.4"]
                 [clojure-csv "2.0.2"]
                 [happygapi "0.4.9"]
                 [instaparse "1.4.12"]
                 [io.forward/yaml "1.0.11"]
                 [medley "1.4.0"]
                 [org.clojure/clojure "1.11.2"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/test.check "1.1.1"]
                 [org.flatland/ordered "1.15.11"]
                 [ring/ring-jetty-adapter "1.12.1"]]
  :plugins [[day8/lein-git-inject "0.0.13"]
            [io.taylorwood/lein-native-image "0.3.1"]]
  :middleware [leiningen.git-inject/middleware]

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
                        "-march=x86-64"
                        "--enable-https"   ;; required to use URI protocol https for talking to Google
                        "--libc=musl"
                        ;; "-g"
                        ;; "-O0"
                        ~(str "--initialize-at-run-time="
                              (clojure.string/join ","
                                                   ["buddy.core.bytes__init"
;                                                    "cli_matic.optionals__init"
                                                    "clojure.core.server__init"
                                                    "clojure.data.json__init"
                                                    "flatland.ordered.map__init"
;                                                    "instaparse.abnf__init"
                                                    "org.apache.http.impl.auth.NTLMEngineImpl"]))
                        "--no-fallback"
                        "--no-server"
                        "--static"]}
  :profiles {:test
             {:native-image {:opts ["--report-unsupported-elements-at-runtime"
                                    "--initialize-at-build-time"
                                    "--verbose"]}}
             
             :uberjar
             {:aot :all
              :native-image {:jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}}

  :main parti-time.cli)
