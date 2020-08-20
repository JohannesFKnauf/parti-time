# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [?.?.?] - Geriatric Go-go (Unreleased)

## [?.?.?] - Feuriger Feierabend (Unreleased)

## [?.?.?] - Effective Ecstasy (Unreleased)

## [1.1.0] - Disco Dance (Unreleased)



### Added
- Feature: New subcommand convert for format conversion
- Feature: Reading tt JSON files
- Contrib: Emacs configuration snippet for an ad-hoc tl-mode with syntax highlighting

## [1.0.0] - Cybernetic Club (2020-08-10)

This version introduces some minor changes to the grammar. The changes break backwards compatibility slightly: The grammar is less tolerant to use of whitespace.

You should not have any trouble fixing invalid files. [instaparse](https://github.com/Engelberg/instaparse)'s excellent parsing errors will guide you.

### Added
- Documentation: Architecture Desicision Record "Faster CLI"
- Documentation: Architecture Desicision Record "Ridig Language"
- CLI frontend driven by [cli-matic](https://github.com/l3nz/cli-matic)

### Changed
- Corner cases in grammar were ruled out to make syntax less ambiguous

### Removed
- Reference dates are not inferred from previous entries any more

## [0.2.0] - Boring Ballermann (2020-07-13)

v0.2.0 is the first release that is published as binary distribution, i.e. native-image compiled with GraalVM.

### Added
- Faster CLI (powered by GraalVM native image)

### Fixed
- Return proper exit code on error
- Print detailed failure message on parse error (instead of misleading error message)

## [0.1.0] - Affengeiler Anfang (2019-10-17)

v0.1.0 is the minimum viable version of parti-time. It already features the most exciting Timeline DSL.

### Usage Advice

* parti-time v0.1.0 can only be run from leiningen

### Added
- Summaries: Project Summary
- Timesheet Report
- Invoice Report
- filename-extension based dispatch of input parsing
- Timeline DSL (a.k.a. tl) parser
- YAML v1 parser
- Integration test stubs

[Unreleased]: https://github.com/JohannesFKnauf/parti-time/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/JohannesFKnauf/parti-time/compare/v0.2.0...v1.0.0
[0.2.0]: https://github.com/JohannesFKnauf/parti-time/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/JohannesFKnauf/parti-time/releases/tag/v0.1.0
