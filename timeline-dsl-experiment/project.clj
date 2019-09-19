(defproject parti-time "0.1.0-SNAPSHOT"
  :description "parti-time is a tool for partitioning timelines."
  :url "https://github.com/JohannesFKnauf/parti-time"
  :min-lein-version "2.8.1"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clojure.java-time "0.3.2"]
                 [instaparse "1.4.10"]]
  :clean-targets ["target"]
  :main tl.cli)
