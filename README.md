# parti-time -- track and partition your time in plain text

parti-time is a tool for partitioning timelines and tracking your time in plain text.

Partitioning implies:
* no undeclared gaps
* no double booking
* 1 single timeline
* strict order

Otherwise keeping a partitioned timeline is similar to classical time-tracking. People use it to remember and document how long they've been working on which projects and occupations.

parti-time enables users to
* keep their timeline in plain text
* create reports about their working hours

## Why should you want to keep a timeline in plain text?

The big advantage of plain text is, that it provides you with well-designed, well-understood generic tooling.
* Editors
* Version Control

Because plain text is such a universal interface it allows every one to select their tooling of choice. 

Also, with plain text in decentralized version control (like git), your time-tracking will work offline without any hassle. It also means you never lose data or get manipulated without noticing. For a client-server SPA you'd have to do a lot to get there.

## Feature roadmap

Status quo:
* tl file based reporting
* editor support for tl files in emacs and vi
* Google Sheet append

2024-Q2:
* tl lenses: Separate storage from tl editor view
* tt-like command line operations

post 2024-Q2:
* Web App
* Tightly scoped access tokens for untrusted customer devices

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

In [`src/itest/resources/examples/v2_tl/TimeTracker.sample.tl`](https://github.com/JohannesFKnauf/parti-time/blob/master/src/itest/resources/examples/v2_tl/TimeTracker.sample.tl) you find a complete 2-day tl sample.

## Getting Started with parti-time's CLI frontend

### Download and install

* Download the static release binary from https://github.com/JohannesFKnauf/parti-time/releases
* Store it in your `PATH`, e.g. in `~/bin` on Ubuntu Linux
* Highly Recommended: Create a symlink or alias `pt`

### Get project summary

    pt projects src/itest/resources/examples/v2_tl/TimeTracker.sample.tl
	
    "Customer X 2019-08" 19.25
    "Customer Z 2019-08" 1.0
    "Metamorphant" 1.5
    "Private" 14.5

For all CLI commands `tl` files, `tt` files and `yaml` files can be used interchangeably. You specify the format using the `--input-format` option. The default format is `tl`.

    pt projects --input-format yaml src/itest/resources/examples/v1_yaml/TimeTracker.sample.yml
	
    "Customer X 2019-08" 19.25
    "Customer Z 2019-08" 1.0
    "Metamorphant" 1.5
    "Private" 14.5

Reports a summary of hours booked per project. Unsorted. This is used for basic cross-checks.

### Get invoice report

    pt invoice-report src/itest/resources/examples/v2_tl/TimeTracker.sample.tl "Customer X 2019-08"
	
	2019-08-12,05:45,17:30,01:45,10:00,"Some Task, Development of Blarz, Interesting other stuff, Architecture Whiteboard Session, Incident Blubb, Decision draft Project Y"
    2019-08-13,05:45,16:15,01:15,09:15,"Roadmap planning, Legacy Stack Analysis, Visualisation of Dependencies, Monitoring stack, Log shipping Integration"

`pt invoice-report` creates a CSV report with a daily summary of booked times on a selected project. The report satisfies the usual german [Gesetz über die Durchführung von Maßnahmen des Arbeitsschutzes zur Verbesserung der Sicherheit und des Gesundheitsschutzes der Beschäftigten bei der Arbeit (Arbeitsschutzgesetz - ArbSchG)](https://www.gesetze-im-internet.de/arbschg/) conditions. This is typically demanded for consulting projects with a labor leasing time & material contract model (cf. [Gesetz zur Regelung der Arbeitnehmerüberlassung](http://www.gesetze-im-internet.de/a_g/)).

Rules typically involve

* Start time on that day is visible (has to be >11h after the last day's end time)
* End time on that day is visible
* Total sum of pauses is visible (has to be >30m per day for adults)
* Total work time is visible (has to be <10h per day)

The list of occupations is meant as a reminder for the involved parties about the work involved.

### Get timesheet

    pt timesheet src/itest/resources/examples/v2_tl/TimeTracker.sample.tl
	
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

### Get days report

There is a report that enables you to check your bookings at a single glance: the days report.

Each day is represented in a single line. The day is separated in 15m slots. Each 15m slot is displayed as a single character. Before the individual days, a key explains the character-project mappings. The days report requires a terminal width of at least 136 characters.

```
pt days src/itest/resources/examples/v2_tl/TimeTracker.sample.tl

  Private
. Customer X 2019-08
_ Customer Z 2019-08
- Metamorphant

               0              3              6              9             12             15             18             21             24
               |              |              |              |              |              |              |              |              |
               |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |    |
2019-08-12 Mon                              . .... ---. .... .... .... ..   .... .... .... ...   ... ..                                
2019-08-13 Tue                              . ....    . .... .... .... ..   .... .... .... .... .--- ____
```

In the presented example, you can quickly make simple observations:
* In general, the days have been used similarly for "Customer X 2019-08"
* The time-window between 0700 and 0745 has been used differently: On 2019-08-12 it was used for Metamorphant, on 2019-08-13 for private activities.
* At the end of 2019-08-13 there has been some Metamorphant activity and some Customer Z activity

### Getting started with the Google Timesheet Integration

See the [dedicated docs](docs/developer_docs/google_sheets_api.md).


# Development

## Prerequisites

* Install Oracle GraalVM >= 22+36.1
* Setup an environment variable `GRAALVM_HOME` pointing to your graalvm installation (e.g. in your `~/.bashrc`), e.g.

```
export GRAALVM_HOME="${HOME}/graalvm-jdk-22+36.1"
```


## Run tests

```
lein test
```

## Build a native-image

```
lein native-image
```

## Create native-image reachability metadata

Create a sample Google spreadsheet and get its spreadsheet ID.

```
lein uberjar
${GRAALVM_HOME}/bin/java -agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image -jar target/parti-time-*-SNAPSHOT-standalone.jar download --google-sheet-id "1qpyC0XzvTcKT6EISywvqESX3A0MwQoFDE8pxBll4hps"
${GRAALVM_HOME}/bin/java -agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image -jar target/parti-time-*-SNAPSHOT-standalone.jar append --google-sheet-id "1qpyC0XzvTcKT6EISywvqESX3A0MwQoFDE8pxBll4hps" --input-parti-file src/itest/resources/examples/v2_tl/TimeTracker.sample.tl
```

Create a native-image and remove the Clojure libraries that appear in the error messages.

```
lein native-image
```

# Known limitations

## Predefined reports

As of now, there are only predefined reports. They are not nice and beautiful, either.

We consider this a major limitation and envision something like a report query language to create arbitrary reports in a crosstab-like fashion.

## No timezones

As of now, all times are limited to local times of a single timezone. There is no handling of different timezones, switching timezones etc. No time travel, as well.

# Media coverage

parti-time has been featured in

* blog article https://metamorphant.de/blog/posts/2019-11-27-parti-time-plain-text-time-tracking-for-nerds/


# Legacy features

## v1: The legacy YAML format

In [```src/itest/resources/examples/v1_yaml/TimeTracker.sample.yml```](https://github.com/JohannesFKnauf/parti-time/blob/master/src/itest/resources/examples/v1_yaml/TimeTracker.sample.yml) you find a 2-day sample v1 time partitioning. You won't need it, except if you have legacy files that you want to convert to a new format.

v1 YAML timelines are deprecated and will be removed in future versions. Use `pt convert` to migrate.
