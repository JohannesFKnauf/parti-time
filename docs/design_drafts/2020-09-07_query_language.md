# Yet another SQL dialect?

Yes! It would be great, if users can build upon their experience in SQL-like query languages.

## Roadmap

1. Build an internal DSL (i.e. Clojure DSL) to structure the query process and prepare introduction of a real SQL dialect. Refactor existing reports to use the internal DSL. Work garbage-in, garbage-out. E.g. return nil on non-existent keys instead of helping the user.
1. Provide static checking of constraints (see below) before query execution and good error messages.
1. Provide a real SQL-like query language. Expose it using a special CLI command `pt select`.
1. Provide query aliases. Extract the existing reports into the default configuration.

On a parallel track the output format can be decoupled from the queries and generic formatters can be implemented. For now, we envision:

* hierarchical formatter
* CSV formatter
* Console Calendar formatter

## Constraints on queries

### chunk by

* TODO How can we know beforehand which queries are valid and which are not?
 * Complex rules! e.g. :day is a valid field only for chunk by :day :hour: ...
 * While :week is fine for :week :day :hour ...

### group by and calculated fields

* TODO it is illegal to select fields which are not part of the group by clause, or have an aggregation function
 * start with producing garbage instead of enforcing
 * continue with static check up front

### available fields in general

* TODO How can we make the DSL explorable?
 * Optimum: Lazily calculated fields
 * first step: precalc all fields

* TODO experiment: have a registry of available fields, report them on error
* TODO experiment: use a lazy-map; this will solve everything most elegant? is it possible to use the current map as context when creating the lazy functions?
 * or use a record/protocol for the fields (and time-windows in general?); this will be more efficient and maybe even clearer
 * thought: potential performance optimization, if needed: precalc field value lookup table, e.g. for hours or days; cf. dim_time

