(ns parti-time.cli
  (:gen-class)
  (:require [cli-matic.core :as cli]
            [parti-time.google-sheets.client]
            [parti-time.google-sheets.timeline]
            [parti-time.input.api]
            [parti-time.input.tl]        ; import for side-effect: multimethod registration
            [parti-time.input.tt]        ; import for side-effect: multimethod registration
            [parti-time.input.yaml]      ; import for side-effect: multimethod registration
            [parti-time.invoice-report]
            [parti-time.output.api]
            [parti-time.output.tl]        ; import for side-effect: multimethod registration
            [parti-time.timesheet]
            [parti-time.report.days]
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
         (sort-by first)
         (map (fn [[project hours]] (str "\"" project "\" " hours)))
         (clojure.string/join "\n")
         println)))

(defn days [{:keys [input-format
                    parti-file]}]
  (parti-time.util.cli/assert-mandatory-argument parti-file)
  (let [timeline (parti-time.input.api/read-timeline input-format parti-file)]
    (->> timeline
         parti-time.report.days/days-report
         println)))

;; the function is named cmd-cat instead of cat, in order not to shadow clojure.core/cat
(defn cmd-cat [{input-files :_arguments
                :keys [input-format
                       output-format
                       output-file]}]
  (->> input-files
       (map (partial parti-time.input.api/read-timeline input-format))
       (apply concat)
       (parti-time.output.api/write-timeline output-format output-file)))

(defn convert [{:keys [input-format
                       input-parti-file
                       output-format
                       output-parti-file]}]
  (parti-time.util.cli/assert-mandatory-argument input-parti-file)
  (parti-time.util.cli/assert-mandatory-argument output-parti-file)
  (->> input-parti-file
      (parti-time.input.api/read-timeline input-format)
      (parti-time.output.api/write-timeline output-format output-parti-file)))

(defn download [{:keys [google-sheet-id
                        output-format
                        output-parti-file]}]
  (parti-time.util.cli/assert-mandatory-argument google-sheet-id)
  (parti-time.google-sheets.client/init!)
  (->> google-sheet-id
       (parti-time.google-sheets.timeline/google-sheet->timeline)
       (parti-time.output.api/write-timeline output-format output-parti-file)))

(defn append [{:keys [google-sheet-id
                      input-format
                      input-parti-file]}]
  (parti-time.util.cli/assert-mandatory-argument google-sheet-id)
  (parti-time.google-sheets.client/init!)
  (let [timeline (parti-time.input.api/read-timeline input-format input-parti-file)]
    (parti-time.google-sheets.timeline/append-timeline! google-sheet-id timeline)
    (println (str "Successfully appended timeline to google sheet '" google-sheet-id "'"))))
 
(defmacro get-version
  "Get project version in compilation phase. Only applicable
  to Leiningen projects."
  []
  `~(System/getProperty "parti-time.version"))

(def APP-CONFIGURATION
  {:app {:command "parti-time"
         :description "Partition your time"
         :version (get-version)}
   :commands [{:command "invoice-report"
               :description "Print an importable invoice report CSV"
               :opts [{:option "input-format" :as "Input file format" :type :string :default "tl"}
                      {:option "parti-file" :as "Input filename" :type :string :short 0}
                      {:option "project" :as "Project" :type :string :short 1}]
               :runs (parti-time.util.cli/with-error-printer invoice-report)}
              {:command "timesheet"
               :description "Print an importable timesheet CSV"
               :opts [{:option "input-format" :as "Input file format" :type :string :default "tl"}
                      {:option "parti-file" :as "" :type :string :short 0}]
               :runs (parti-time.util.cli/with-error-printer timesheet)}
              {:command "projects"
               :description "Print a report about the hours spent by project"
               :opts [{:option "input-format" :as "Input file format" :type :string :default "tl"}
                      {:option "parti-file" :as "" :type :string :short 0}]
               :runs (parti-time.util.cli/with-error-printer projects)}
              {:command "days"
               :description "Print a compact report about how days have been spent."
               :opts [{:option "input-format" :as "Input file format" :type :string :default "tl"}
                      {:option "parti-file" :as "" :type :string :short 0}]
               :runs (parti-time.util.cli/with-error-printer days)}
              {:command "cat"
               :description "Read multiple input files and concatenate them to a single output file -- Beware! Performs no validation of the resulting output! Garbage-in, garbage-out."
               :opts [{:option "input-format" :as "Input file format" :type :string :default "tl"}
                      {:option "output-format" :as "Output file format" :type :string :default "tl"}
                      {:option "output-file" :as "Output file name. Defaults to '-', i.e. output to STDOUT." :type :string :default "-"}]
               :runs (parti-time.util.cli/with-error-printer cmd-cat)}
              {:command "convert"
               :description "Convert between parti-time formats"
               :opts [{:option "input-format" :as "Input file format" :type :string :default "tl"}
                      {:option "input-parti-file" :as "" :type :string :short 0}
                      {:option "output-format" :as "Input file format" :type :string :default "tl"}
                      {:option "output-parti-file" :as "" :type :string :short 1}]
               :runs (parti-time.util.cli/with-error-printer convert)}
              {:command "download"
               :description "Download your times from a Google Sheet"
               :opts [{:option "google-sheet-id" :as "" :type :string}
                      {:option "output-format" :as "" :type :string :default "tl"}
                      {:option "output-parti-file" :as "" :type :string :default "-"}]
               :runs (parti-time.util.cli/with-error-printer download)}
              {:command "append"
               :description "Append your times to a Google Sheet"
               :opts [{:option "google-sheet-id" :as "" :type :string}
                      {:option "input-format" :as "" :type :string :default "tl"}
                      {:option "input-parti-file" :as "" :type :string :short 0}]
               :runs (parti-time.util.cli/with-error-printer append)}]})

(defn -main [& args]
  (cli-matic.core/run-cmd args APP-CONFIGURATION))
