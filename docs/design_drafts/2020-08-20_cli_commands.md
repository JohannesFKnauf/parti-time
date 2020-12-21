# What would a good CLI look and feel like?

* Best in class bash completion
* Focus on the necessary arguments, provide the defaults by config
* Shared mental models between features
* Simple canonical translation to a RESTful API (cf. [kubectl](https://github.com/kubernetes/kubernetes/tree/master/cmd/kubectl) or [docker cli](https://github.com/docker/cli))

# Experimenting with Concrete Examples

## Output format selection

```bash
parti-time report ... --format console ...
parti-time report ... --output-format console ...
parti-time convert --in-format tt --in-file - --out-format tl --out-file - 
parti-time convert --input-format tt --input-file - --output-format tl --output-file - 
```
`--format console` should be default

## Report time window selection

```bash
parti-time report ... --from 2020-08-01 --to 2020-08-31
parti-time report ... --format console --for 2020-08
parti-time report ... --format console --for 2020/CW34
```

Convenience aliases

```bash
parti-time report ... today
parti-time report ... yesterday
parti-time report ... last month
parti-time report ... this month
```

## Report grain selection

```bash
# as option
parti-time report ... --grain day
parti-time report ... --grain hour

# as mini-DSL for describing hierarchies
parti-time report day project
parti-time report hour project
parti-time report project day
```

## Report DSLs

```bash
# as mini-DSL for describing complex queries

# filter: =
# group by: /
parti-time report project='Customer X 2020-08' /day /project

# selecting the projection last
parti-time report project='Customer X 2020-08' /day /project : duration/hour

# selecting the projection first
parti-time report day,project,duration : project='Customer X 2020-08'

# as series of options
parti-time report --filter=project='Customer X 2020-08' --group-by=day --group-by=project

# as plain clojure
parti-time report '(->> data (filter #(= (:project %1) "Customer X 2020-08")) (group-by ...))'

# SQL like DSL (cf. Jira QL)
parti-time report project='Customer X 2020-08' BY day BY project

parti-time select day, project, sum(duration) WHERE project='Customer X 2020-08' GROUP BY day, project

# querying plain entries (time-windows?)
parti-time report * WHERE occupation ~ 'support'

# time-windows could be selected in a similar fashion
parti-time report * WHERE 2020-08-01 <= date <= 2020-08-31
parti-time report * WHERE month = 2020-08
```

Existing reports using these languages:
```bash
# invoice-report
parti-time report date AS date, min(start-time) AS business_day_start_time, max(end-time) AS business_day_end_time, duration_between(min(start-time), max(end-time))-sum(duration) AS break_minutes, sum(duration) AS work_minutes, JOIN(occupations) WHERE MONTH(date) = 2020-08 AND project = 'Customer X 2020-08' GROUP BY date

# timesheet
parti-time report date AS date, format("HH:mm", start-time) AS start-time, format("HH:mm", end-time) AS end-time, '' AS duration, JOIN(',', occupations), project AS project

# projects
parti-time report project, sum(duration) GROUP BY project
parti-time report project, sum(duration) AS hours GROUP BY project ORDER BY hours
```

Points:
* Column selection / projection
* Aggregation
* Filtering
* Formatting single columns

SPLIT BY / CHUNK BY: The splitting into minimal chunks is a special topic. A timeline, in principle, is continuous. Chunking can be useful without grouping, e.g. when just looking into all entries for a time range and chunking by day in oder to resolve time windows overlapping with midnight. It should have a separate term.


## Report aliases

Idea: User-definable query aliases. Standard reports become sample entries in the docs. Same formatters for all queries.

Some useful default reports with good aliases would be helpful

* project days (single project / day + totals)
* invoice-report
* projects (totals for all projects in timeframe)
* timesheet
* occuptions in a project

```bash
parti-time reports ...
# vs.
parti-time select ...
```

# Experimenting with output examples

## CSV output

* As usual
* For import into spreadsheets etc.

## Hierarchical output

* Deduplication
* Table spacing
* For interactive use

```
2020-08-04    Customer X 2020-08      8.50h
              Private                15.50h 
2020-08-05    Private                24.00h
2020-08-06    Customer X 2020-08      6.25h
```

```
Customer X 2020-08    2020-08-04      8.50h
                      2020-08-06      6.25h
Private               2020-08-04     15.50h 
                      2020-08-05     24.00h
```

# Semantic coloring and style

Semantics (and default style)

* Highlight (bold?)
* Dimension 1 2 3? (slightly different colors)
* All good (green)
* Warn (yellow)
* Alarm (red)
