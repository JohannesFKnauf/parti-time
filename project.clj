(defproject parti-time "_"
  :description "parti-time is a tool for partitioning timelines."
  :url "https://github.com/JohannesFKnauf/parti-time"
  :min-lein-version "2.10.0"
  :global-vars {*warn-on-reflection* true}
  :dependencies [[cheshire "6.0.0"]
                 [cli-matic "0.5.4"]
                 [clojure-csv "2.0.2"]
                 [dev.weavejester/medley "1.8.1"]
                 [happygapi "0.4.10"]
                 [instaparse "1.5.0"]
                 [io.forward/yaml "1.0.11"]
                 [org.clojure/clojure "1.12.0"]
                 [org.clojure/data.json "2.5.1"]
                 [org.clojure/test.check "1.1.1"]
                 [org.flatland/ordered "1.15.12"]
                 [ring/ring-jetty-adapter "1.14.1"]]
  :plugins [[me.arrdem/lein-git-version "2.0.8"]
            [io.taylorwood/lein-native-image "0.3.1"]]

  :git-version {:status-to-version
                (fn [{:keys [tag version branch ahead ahead? dirty? ref-short] :as git}]
                  (let [[tag-match prefix patch] (re-find #"(?<=v)(\d+\.\d+)\.(\d+)" tag)]
                  (assert tag-match
                          "Tag is assumed to be a raw SemVer version with a 'v' prefix")
                  (if (and tag-match (not ahead?) (not dirty?))
                    tag-match
                    (let [patch            (Long/parseLong patch)
                          patch+           (inc patch)]  ; SNAPSHOT versions are +1 patch version ahead of the latest stable release, i.e. the latest tag
                      (format "%s.%d-SNAPSHOT" prefix patch+)))))}
  
  :source-paths ["src/main/clj"]
  :test-paths ["src/test/clj" "src/itest/clj"]
  :test-selectors {:default (complement :integration)
                   :integration :integration
                   :all (constantly true)}
  :resource-paths ["src/main/resources"]
  :dev {:resource-paths ["src/test/resources" "src/itest/resources"]}
  :aliases {"itest" ["test" ":integration"]
            "print-binary" ["run" "-m" "parti-time.lein.print-binary" :project/name :project/version "linux" "x86_64"]}
  :clean-targets ["target"]

  :native-image {:name "parti-time"  ; static output file name instead of the varying default: project name + project version
                 :opts ["--initialize-at-build-time"
                        "-march=x86-64"
                        "--enable-https"   ;; required to use URI protocol https for talking to Google
                        "--libc=musl"
                        "--no-fallback"
                        "--no-server"
                        "--static"
                         ~(str "--initialize-at-run-time="
                               (clojure.string/join ","
                                                    ["buddy.core.bytes__init"
                                                     "clojure.core.server__init"
                                                     "clojure.data.json__init"
                                                     "flatland.ordered.map__init"
                                                     "happy.oauth2__init"
                                                     "happy.oauth2_capture_redirect__init"
                                                     "instaparse.abnf__init"
                                                     "org.apache.http.impl.auth.NTLMEngineImpl"]))]}
  :profiles {:development  ; lein with-profile +development native-image
             {:native-image {:opts ["--report-unsupported-elements-at-runtime"
                                    "--verbose"
                                    "-g"
                                    "-O0"]}}
             
             :release  ; lein with-profile +release native-image
             {:aot :all
              :native-image {:jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                        "-Dclojure.compiler.elide-meta=[:doc :file :line :added]"]
                             :opts ["-O3"]}}
             :uberjar
             {:aot :all}}

  :main parti-time.cli)
