# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - Unreleased

### Added
- Experimental: Comments in tl format
- Faster CLI (powered by GraalVM native image)

### Changed

### Removed

### Fixed
- Return proper exit code on error
- Print detailed failure message on parse error (instead of misleading error message)

## [0.1.0] - 2019-10-17
### Added
- Summaries: Project Summary
- Timesheet Report
- Invoice Report
- filename-extension based dispatch of input parsing
- Timeline DSL (a.k.a. tl) parser
- YAML v1 parser
- Integration test stubs

[Unreleased]: https://github.com/JohannesFKnauf/parti-time/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/JohannesFKnauf/parti-time/compare/v0.1.0...v1.0.0
[0.1.0]: https://github.com/JohannesFKnauf/parti-time/releases/tag/v0.1.0
