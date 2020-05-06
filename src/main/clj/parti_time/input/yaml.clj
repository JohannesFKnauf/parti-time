(ns parti-time.input.yaml
  (:require [tick.alpha.api :as tick]
            [yaml.core :as yaml]
            [parti-time.input.api :as api]))

(defn parse-iso-time [timestamp]
  (tick/date-time timestamp))

(defn import-yaml-timeslice [{:keys [starting_from project location occupation] :or {project "" location "" occupation ""}}]
  {:start-time (parse-iso-time starting_from)
   :project project
   :location location
   :occupation (clojure.string/split occupation #",\s*")})

(defn import-yaml-timeline [yaml-timeline]
  (->> yaml-timeline
       (yaml/parse-string)
       (map import-yaml-timeslice)))

(defn read-yaml-timeline [filename]
  (-> filename
      slurp
      import-yaml-timeline))

(defmethod api/read-timeline "yml" [filename]
  (read-yaml-timeline filename))

(defmethod api/read-timeline "yaml" [filename]
  (read-yaml-timeline filename))
