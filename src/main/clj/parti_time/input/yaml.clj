(ns parti-time.input.yaml
  (:require [yaml.core :as yaml]
            [parti-time.input.api :as api]
            [parti-time.util.time :as time]))

(defn import-yaml-timeslice [{:keys [starting_from project location occupation] :or {project "" location "" occupation ""}}]
  {:start-time (time/parse-iso-date-time starting_from)
   :project project
   :location location
   :occupations (clojure.string/split occupation #",\s*")})

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
