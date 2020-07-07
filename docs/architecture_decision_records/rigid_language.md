# Background for Decision "Rigid Language"

When designing the timeline DSL, there are 2 ways of taking it to the extreme:

* Design it flexible, with a lot of redundant syntactic elements and freedom around whitespace etc.
* Design it rigid and force the users to comply with 1 single, opinionated, standard way of doing things.

During experimentation I implemented features like free-form comments (starting with #, filtered in a phase before parsing; quite similar to usual Unix script languages). I also noticed that most syntactical mistakes are whitespace issues. So, what should we do? Be tolerant or fix issues at the root? What is better for the user in the long-term?

Furthermore, another feature idea appeared on the horizon: Implement the timeline DSL as a bidirectional serialization/deserialization format. Previous experience with [XML](https://www.w3.org/XML/), [JSON](https://www.json.org/json-en.html), [YAML](https://yaml.org/) and especially YAML implementations like
* [https://godoc.org/gopkg.in/yaml.v3](gopkg.in/yaml.v3) for [Go](https://golang.org/),
* [SnakeYAML](https://bitbucket.org/asomov/snakeyaml/) for the [JVM](https://en.wikipedia.org/wiki/Java_virtual_machine) and
* [PyYAML](https://pypi.org/project/PyYAML/) for [Python](https://www.python.org/)
taught me the lesson that language elements without semantic meaning (comments, whitespace, ...) and ambiguity about how to express something make a bidirectional serialization/deserialization approach difficult to handle. In the end you will lose the benefits of freedom and syntax sugar. You will arrive at a canonical form that you could have started with, in the first place.

# Decision

Effective starting by: 2020-07-07

The timeline DSL 
* should be described completely by the EBNF grammar,
* should have 1 and only 1 canonical form,
* should be as unambiguous as possible.
