(ns parti-time.cli
  (:gen-class)
  (:require [cli-matic.core :as cli]
            [parti-time.input.api]
            [parti-time.input.tl]        ; import for side-effect: multimethod registration
            [parti-time.input.tt]        ; import for side-effect: multimethod registration
            [parti-time.input.yaml]      ; import for side-effect: multimethod registration
            [parti-time.invoice-report]
            [parti-time.output.api]
            [parti-time.output.tl]        ; import for side-effect: multimethod registration
            [parti-time.timesheet]
            [parti-time.summary]
            [parti-time.util.cli]))

(defn invoice-report [{:keys [input-format
                              parti-file
                              project]}]
  (parti-time.util.cli/assert-mandatory-argument parti-file)
  (parti-time.util.cli/assert-non-empty-argument project)
  (let [timeline (parti-time.input.api/read-timeline input-format parti-file)]
    (println (parti-time.invoice-report/csv-report timeline project))))

(defn timesheet [{:keys [input-format
                         parti-file]}]
  (parti-time.util.cli/assert-mandatory-argument parti-file)
  (let [timeline (parti-time.input.api/read-timeline input-format parti-file)]
    (println (parti-time.timesheet/csv-report timeline))))

(defn projects [{:keys [input-format
                        parti-file]}]
  (parti-time.util.cli/assert-mandatory-argument parti-file)
  (let [timeline (parti-time.input.api/read-timeline input-format parti-file)]
    (->> timeline
         parti-time.summary/project-summary
         (clojure.string/join "\n")
         println)))

(defn convert [{:keys [input-format
                       input-parti-file
                       output-format
                       output-parti-file]}]
  (parti-time.util.cli/assert-mandatory-argument input-parti-file)
  (parti-time.util.cli/assert-mandatory-argument output-parti-file)
  (->> input-parti-file
      (parti-time.input.api/read-timeline input-format)
      (parti-time.output.api/write-timeline output-format output-parti-file)))


(def APP-CONFIGURATION
  {:app {:command "parti-time"
         :description "Partition your time"
         :version "1.1.0-SNAPSHOT"}
   :commands [{:command "invoice-report"
               :description ""
               :opts [{:option "input-format" :as "Input file format" :type :string :default "tl"}
                      {:option "parti-file" :as "Input filename" :type :string :short 0}
                      {:option "project" :as "Project" :type :string :short 1}]
               :runs (parti-time.util.cli/with-error-printer invoice-report)}
              {:command "timesheet"
               :description ""
               :opts [{:option "input-format" :as "Input file format" :type :string :default "tl"}
                      {:option "parti-file" :as "" :type :string :short 0}]
               :runs (parti-time.util.cli/with-error-printer timesheet)}
              {:command "projects"
               :description ""
               :opts [{:option "input-format" :as "Input file format" :type :string :default "tl"}
                      {:option "parti-file" :as "" :type :string :short 0}]
               :runs (parti-time.util.cli/with-error-printer projects)}
              {:command "convert"
               :description "Convert between parti-time formats"
               :opts [{:option "input-format" :as "Input file format" :type :string :default "tl"}
                      {:option "input-parti-file" :as "" :type :string :short 0}
                      {:option "output-format" :as "Input file format" :type :string :default "tl"}
                      {:option "output-parti-file" :as "" :type :string :short 1}]
               :runs (parti-time.util.cli/with-error-printer convert)}]})

(defn -main [& args]
  (cli-matic.core/run-cmd args APP-CONFIGURATION))
