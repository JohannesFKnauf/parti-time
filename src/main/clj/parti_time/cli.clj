(ns parti-time.cli
  (:gen-class)
  (:require [parti-time.input.api]
            [parti-time.input.tl]
            [parti-time.input.yaml]
            [parti-time.invoice-report]
            [parti-time.timesheet]
            [parti-time.summary]))

(defn invoice-report [& args]
  (let [parti-filename (nth args 0)
        project-name (nth args 1)
        timeline (parti-time.input.api/read-timeline parti-filename)]
    (println (parti-time.invoice-report/csv-report timeline project-name))))

(defn timesheet [& args]
  (let [parti-filename (nth args 0)
        timeline (parti-time.input.api/read-timeline parti-filename)]
    (println (parti-time.timesheet/csv-report timeline))))

(defn projects [& args]
  (let [parti-filename (nth args 0)
        timeline (parti-time.input.api/read-timeline parti-filename)]
    (->> timeline
         parti-time.summary/project-summary
         (clojure.string/join "\n")
         println)))

(defn -main [& args]
  (let [[subcommand & subargs] args]
    (case subcommand
      "invoice-report" (apply invoice-report subargs)
      "timesheet" (apply timesheet subargs)
      "projects" (apply projects subargs)
      (println "No valid subcommand given. Exiting."))))
