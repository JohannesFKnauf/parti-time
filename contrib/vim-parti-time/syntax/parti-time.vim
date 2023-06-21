" syntax/parti-time.vim
" Vim syntax file
" Language: parti-time
" Maintainer: Rolf Halmen <rolf@hal-men.com>
" Last Change: 2023-06-21
" Remark: parti-time repo: https://github.com/JohannesFKnauf/parti-time

if exists("b:current_syntax")
  finish
endif

"" the date in ISO8601, at the beginning of a line
syntax match partiDate "^\v\d{4}-\d{2}-\d{2}"
highlight default link partiDate Structure

"" categories of entries
syntax match partiCategory "\v \zs.+" contained
highlight default link partiCategory Statement

"" the time in military notation, also at the start of a line
syntax match partiTime "^\v\d{4}\ze " nextgroup=partiCategory contained
highlight default link partiTime Number

"" partiLines are lines that contain both a partiTime and a partiCategory
syntax match partiLine "\v^\d{4} .+" contains=partiTime,partiCategory

"" notes, that get attached to the tracked interval
syntax match partiNote "^\v {5}.+"
highlight default link partiNote SpecialComment

let b:current_syntax = 'parti-time'
