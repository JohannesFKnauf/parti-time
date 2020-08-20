(require 'generic-x)

;; poor man's tl emacs mode
;; provides rudimentary syntax highlighting

(define-generic-mode 'tl-mode
  '("#")                           ;; comments start with '#'
  '()                              ;; no special keywords
  '(("^[[:digit:]]\\{4\\}-[[:digit:]]\\{2\\}-[[:digit:]]\\{2\\}" . 'font-lock-function-name-face)     ;; reference dates
    ("^[[:digit:]]\\{4\\}" . 'font-lock-variable-name-face)                                           ;; military times at begin of a timeslice
    ("^[[:digit:]]\\{4\\} \\(.*\\)$" 1 'font-lock-string-face))                                       ;; Category of timeslice
  '("\\.tl$")                      ;; files for which to activate this mode 
  nil                              ;; other functions to call
  "A mode for tl files"            ;; doc string for this mode
  )

(provide 'tl-mode)
