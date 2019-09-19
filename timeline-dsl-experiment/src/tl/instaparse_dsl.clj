(ns tl.instaparse-dsl
  (:require [instaparse.core :as insta]
            [java-time]))

(insta/defparser timeline-parser
  "./resources/tl/instaparse_grammar.ebnf")

(defn ast->entries
  "Transforms the abstract syntax tree of a timeline DSL parsing result into a proper timeline."
  ([ast]
   (insta/transform {:reference-date #(hash-map :reference-date (java-time/local-date "yyyy-MM-dd" %))
                     :hhmm-time #(hash-map :time (java-time/local-time "HHmm" %))
                     :subject #(hash-map :subject %)
                     :detail #(hash-map :detail %)
                     :entry merge
                     :timeline vector}
                    ast)))


(defn work-timeline [parsed-timeline]
  )
