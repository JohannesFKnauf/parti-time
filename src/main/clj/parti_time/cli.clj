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
            [parti-time.summary]))

(defn assert-mandatory-argument [arg arg-name]
  (when (not arg) (throw (RuntimeException. (str "Mandatory argument " arg-name " not provided.")))))

(defn assert-non-empty-argument [arg arg-name]
  (when (or (not arg)
            (= arg "")) (throw (RuntimeException. (str "Argument " arg-name " must not be empty.")))))

(defn invoice-report [{:keys [parti-file project]}]
  (assert-mandatory-argument parti-file "parti-file")
  (assert-non-empty-argument project "project")
  (let [timeline (parti-time.input.api/read-timeline parti-file)]
    (println (parti-time.invoice-report/csv-report timeline project))))

(defn timesheet [{:keys [parti-file]}]
  (assert-mandatory-argument parti-file "parti-file")
  (let [timeline (parti-time.input.api/read-timeline parti-file)]
    (println (parti-time.timesheet/csv-report timeline))))

(defn projects [{:keys [parti-file]}]
  (assert-mandatory-argument parti-file "parti-file")
  (let [timeline (parti-time.input.api/read-timeline parti-file)]
    (->> timeline
         parti-time.summary/project-summary
         (clojure.string/join "\n")
         println)))

(defn convert [{:keys [input-parti-file
                       output-parti-file]}]
  (assert-mandatory-argument input-parti-file "input-parti-file")
  (assert-mandatory-argument output-parti-file "output-parti-file")
  (->> input-parti-file
      (parti-time.input.api/read-timeline)
      (parti-time.output.api/write-timeline output-parti-file)))

(defn with-error-printer [command]
  (fn [args]
    (try
      (command args)
      (catch Exception ex
        (binding [*out* *err*]
          (println "An error occured:")
          (println (.getMessage ex)))
        1   ; return code for cli-matic
        ))))


(def APP-CONFIGURATION
  {:app {:command "parti-time"
         :description "Partition your time"
         :version "1.1.0-SNAPSHOT"}
   :commands [{:command "invoice-report"
               :description ""
               :opts [{:option "parti-file" :as "" :type :string :short 0}
                      {:option "project" :as "" :type :string :short 1}]
               :runs (with-error-printer invoice-report)}
              {:command "timesheet"
               :description ""
               :opts [{:option "parti-file" :as "" :type :string :short 0}]
               :runs (with-error-printer timesheet)}
              {:command "projects"
               :description ""
               :opts [{:option "parti-file" :as "" :type :string :short 0}]
               :runs (with-error-printer projects)}
              {:command "convert"
               :description "Convert between parti-time formats"
               :opts [{:option "input-parti-file" :as "" :type :string :short 0}
                      {:option "output-parti-file" :as "" :type :string :short 1}]
               :runs (with-error-printer convert)}]})

(defn -main [& args]
  (cli-matic.core/run-cmd args APP-CONFIGURATION))
