# parti-time -- track and partition your time in plain text

parti-time is a tool for partitioning timelines and tracking your time in plain text.

Partitioning implies:
* no undeclared gaps
* no double booking
* 1 timeline per tracked dimension (location, occupation, ...)
* strict order

Otherwise keeping a partitioned timeline is similar to classical time-tracking. People use it to remember e.g.
* how long they were working on which projects and occupations
* which locations they visited
* ...

parti-time enables users to
* keep their timeline in plain text
* create reports about their working hours

## A comment about the roadmap

In experiment v1 we started with a baby-step in that direction: Time-slices are declared only by start timestamp instead. The end timestamp is implicit and is derived from the start timestamp of the following time record.

In v1 we still lack the separation of dimensions, i.e. project, location, occupation are all part of the same time record. As a consequence, they are also tracked in the same file.

### v1: The legacy YAML format

In [```src/itest/resources/examples/v1_yaml/TimeTracker.sample.yml```](https://github.com/JohannesFKnauf/parti-time/blob/master/src/itest/resources/examples/v1_yaml/TimeTracker.sample.yml) you find a 2-day sample v1 time partitioning.

### v2: tl, the timeline DSL

tl has been designed to be
* concise
* precise
* human-editable with ease
* human-readable
* simple to parse
* visually clean

Here is a sample day's declaration as timeline:

    2019-08-12
    0545 Customer X 2019-08
         Some Task
    0700 Metamorphant
         Proof-Reading Metamorphant Blog
    0745 Customer X 2019-08
         Development of Blarz, Interesting other stuff
    1130 Private
         Lunch Break
    1200 Customer X 2019-08
         Architecture Whiteboard Session, Incident Blubb
    1545 Private
         Reading Awesome Clojure
    1615 Customer X 2019-08
         Decision draft Project Y
    1730 Private

In [```src/itest/resources/examples/v2_tl/TimeTracker.sample.tl```](https://github.com/JohannesFKnauf/parti-time/blob/master/src/itest/resources/examples/v2_tl/TimeTracker.sample.tl) you find a tl version of the complete 2-day sample time partitioning for the project dimension.

## Getting Started with CLI frontend

For all CLI commands tl files and yaml files can be used interchangeably. parti-time decides based on filename extension how to properly read the file.

### Get project summary

    lein run projects examples/v1_yaml/TimeTracker.sample.yml
	
    ["Customer X 2019-08" 19.25]
    ["Metamorphant" 1.5]
    ["Private" 14.5]
    ["Customer Z 2019-08" 1.0]

Reports a summary of hours booked per project. Unsorted. This is used for basic cross-checks.

### Get invoice report

    lein run invoice-report examples/v1_yaml/TimeTracker.sample.yml "Customer X 2019-08"
	
	2019-08-12,05:45,17:30,01:45,10:00,"Some Task, Development of Blarz, Interesting other stuff, Architecture Whiteboard Session, Incident Blubb, Decision draft Project Y"
    2019-08-13,05:45,16:15,01:15,09:15,"Roadmap planning, Legacy Stack Analysis, Visualisation of Dependencies, Monitoring stack, Log shipping Integration"

Creates a CSV report with a daily summary of booked times on a selected project. The report satisfies the usual german [https://www.gesetze-im-internet.de/arbschg/](Gesetz über die Durchführung von Maßnahmen des Arbeitsschutzes zur Verbesserung der Sicherheit und des Gesundheitsschutzes der Beschäftigten bei der Arbeit (Arbeitsschutzgesetz - ArbSchG)) conditions. This is typically demanded for consulting projects with a labor leasing time & material contract model (cf. [http://www.gesetze-im-internet.de/a_g/](Gesetz zur Regelung der Arbeitnehmerüberlassung)).

Rules typically involve

* Start time on that day is visible (has to be >11h after the last day's end time)
* End time on that day is visible
* Total sum of pauses is visible (has to be >30m per day for adults)
* Total work time is visible (has to be <10h per day)

The list of occupations is meant as a reminder for the involved parties about the work involved.

### Get timesheet

    lein run timesheet TimeTracker.sample.yml
	
    2019-08-12,05:45,07:00,,Some Task,Customer X 2019-08
    2019-08-12,07:00,07:45,,Proof-Reading Metamorphant Blog,Metamorphant
    2019-08-12,07:45,11:30,,"Development of Blarz, Interesting other stuff",Customer X 2019-08
    2019-08-12,11:30,12:00,,Lunch Break,Private
    2019-08-12,12:00,15:45,,"Architecture Whiteboard Session, Incident Blubb",Customer X 2019-08
    2019-08-12,15:45,16:15,,Reading Awesome Clojure,Private
    2019-08-12,16:15,17:30,,Decision draft Project Y,Customer X 2019-08
    2019-08-12,17:30,05:45,,,Private
    2019-08-13,05:45,07:00,,Roadmap planning,Customer X 2019-08
    2019-08-13,07:00,07:45,,Reading Wonderful Clojure,Private
    2019-08-13,07:45,11:30,,"Legacy Stack Analysis, Visualisation of Dependencies",Customer X 2019-08
    2019-08-13,11:30,12:00,,Lunch Break,Private
    2019-08-13,12:00,16:15,,"Monitoring stack, Log shipping Integration",Customer X 2019-08
    2019-08-13,16:15,17:00,,Phone call with customer Z,Metamorphant
    2019-08-13,17:00,18:00,,"Automated DEV host setup, Build pipelines",Customer Z 2019-08

The timesheet feature generates a report that follows the usual format of classical timesheets, i.e. is time-slice oriented.


# Known limitations

## CLI util Startup time

Startup time is awkwardly slow for a command line util.

We consider this a minor limitation as the command line functions are used with low frequency as compared to the editing of your timeline. The editing performance is limited only by your editor of choice.

We might still want to fix it. [GraalVM Native Image](https://www.graalvm.org/docs/reference-manual/native-image/) and [lein-native-image](https://github.com/taylorwood/lein-native-image) might improve the situation a bit. Going to a fast-client-/cached-server-pattern would be the best and also enable other user interfaces than the CLI. See also the pattern of how [groovyserv](https://kobo.github.io/groovyserv/) does it: CLI client written in [go](https://golang.org/) and server in [Apache Groovy](http://groovy-lang.org/). See also prior art from the lisp community: [emacs daemon and emacsclient](https://www.emacswiki.org/emacs/EmacsAsDaemon).

## Predefined reports

As of now, there are only predefined reports. They are not nice and beautiful, either.

We consider this a major limitation and envision something like a report query language to create arbitrary reports in a crosstab-like fashion.
