(defproject parti-time "1.2.1-SNAPSHOT"
  :description "parti-time is a tool for partitioning timelines."
  :url "https://github.com/JohannesFKnauf/parti-time"
  :min-lein-version "2.11.1"
  :global-vars {*warn-on-reflection* true}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/test.check "1.1.1"]
                 [org.flatland/ordered "1.15.11"]
                 [io.forward/yaml "1.0.11"]
                 [cli-matic "0.5.4"]
                 [clojure-csv "2.0.2"]
                 [com.google.api-client/google-api-client "2.3.0"]
                 [com.google.oauth-client/google-oauth-client-jetty "1.34.1"]
                 [com.google.apis/google-api-services-sheets "v4-rev20230815-2.0.0"]
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
                        "-march=x86-64"
                        ~(str "--initialize-at-run-time="
                              (clojure.string/join ","
                                                   ["sun.awt.dnd.SunDropTargetContextPeer$EventDispatcher"
                                                    "sun.awt.X11.XDataTransferer"
                                                    "sun.awt.X11.XDnDConstants"
                                                    "sun.awt.X11.MotifDnDConstants"
                                                    "sun.awt.X11.MotifDnDConstants"
                                                    "sun.awt.X11.WindowPropertyGetter"
                                                    "sun.awt.X11.XWindow"
                                                    "sun.awt.X11.XSelection"
                                                    "sun.awt.X11.XSystemTrayPeer"
                                                    "sun.awt.X11.XToolkitThreadBlockedHandler"
                                                    "sun.awt.X11.XWM"
                                                    "sun.awt.X11GraphicsConfig"
                                                    "sun.java2d.Disposer"
                                                    "sun.font.PhysicalStrike"
                                                    "sun.font.StrikeCache"
                                                    "sun.font.StrikeCache$WeakDisposerRef"
                                                    "sun.font.SunFontManager"
                                                    "com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp"
                                                    "com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp$DefaultBrowser"
                                                    "com.google.api.client.http.javanet.NetHttpTransport"]))
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
