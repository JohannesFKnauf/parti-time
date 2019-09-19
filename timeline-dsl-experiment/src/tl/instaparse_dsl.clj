(ns tl.instaparse-dsl
  (:require [instaparse.core :as insta]))

(insta/defparser timeline-parser
  "./resources/tl/instaparse_grammar.ebnf")

(defn parsed->timeline [parsing-result]
  )
