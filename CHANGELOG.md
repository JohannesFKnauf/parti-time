# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [?.?.?] - Knorker Kaffeeklatsch (Unreleased)

## [?.?.?] - Jolly Jubilee (Unreleased)

## [?.?.?] - Irresistable Inn (Unreleased)

## [1.5.1] - Hyper Happening (2025-06-02)
- Fix: #40 Improve stability of Google Sheet append
- Fix: #38 Assert that user-provided google sheet ID is non-empty

## [1.5.0] - Hyper Happening (2025-05-29)
- Feature: #31 Diff Google Sheets timeline version with local timeline version
- Feature: #33 Improved feedback about append result
- Feature: Use random ports for Google Auth (instead of a static default port)
- Feature: Fill Google Sheet duration column formula on append
- Feature: Apply Google Sheet formats for all appended columns
- Feature: Global option --debug to print stacktraces in case of an exception
- Fix: Use a proper grammar for A1 notation ranges instead of a half-baken bug-ridden regex

## [1.4.1] - Geriatric Go-go (2024-10-21)

- Fix: Show trailing timebits in days report even if they don't end in a full hour

## [1.4.0] - Geriatric Go-go (2024-05-19)

- Feature: Days Report

## [1.3.3] - Feuriger Feierabend (2024-04-14)

- Fix: Use lein plugin me.arrdem/lein-git-version instead of day8/lein-git-inject to fix lein
- Improvement: Clean up native-image build profiles
- Improvement: Let native-image binaries include the project version in their name

## [1.3.2] - Feuriger Feierabend (2024-04-14)

- Fix: Resolve configuration file paths at runtime

## [1.3.1] - Feuriger Feierabend (2024-04-13)

- Fix: Change to happygapi as Gsheets API client library to let graalvm binaries support google sheets
- Improvement: Derive version from git tag

## [1.3.0] - Feuriger Feierabend (2024-03-29)

- Feature: First version of Google Sheets integration (download+append)
- Improvement: github actions for tag builds build the native-image release

## [1.2.0] - Effective Ecstasy (2024-02-24)

### Added

- Feature: Command cat for concatenating multiple files
- Feature: Read from STDIN by providing filename "-"
- Feature: Write to STDOUT by providing filename "-"
- Fix: Remove wrapping brackets '[...]' from projects command output

## [1.1.0] - Default Disco (2024-02-13)

### Added
- Feature: Default to "tl" format for files without extension
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
