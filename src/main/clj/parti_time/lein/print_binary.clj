(ns parti-time.lein.print-binary)

(defn -main
  "Little helper for printing the desired output binary name.
  That name is dynamic and depends on the lein project map (project
  name, project version, OS, arch). By writing our own main function,
  we can call it with selected unspliced project map values with an
  alias in project.clj."
  [& args]
  (println (clojure.string/join "-" args)))
