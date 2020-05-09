# Background for Feature "Faster CLI"

The JVM has a big problem for CLI applications: slow startup time. The goal of the feature "Faster CLI" is to improve User Experience by enabling parti-time commands to run fast. The benchmark for "fast" are 

* comparable native programs developed e.g. in C, C++, Rust or Golang and
* comparable programs in common script languages e.g. Python, Ruby, Perl.

I considered this a minor limitation as the command line functions are used with low frequency as compared to the editing of your timeline. The editing performance is limited only by your editor of choice.

However, it annoyed me anyways, so I just tackled it.

# Considered solution alternatives

## Alternative 1: Long-running server + native client

The startup time problem is only relevant, if you start a new process for each command. One strategy to overcome the issue, is to split the JVM part into a long-running server process and write a client with fast startup time in a native language.

Examples:
* [groovyserv](https://kobo.github.io/groovyserv/) combines a CLI client written in [go](https://golang.org/) and a server in [Apache Groovy](http://groovy-lang.org/).
* [GNU Emacs](https://www.gnu.org/software/emacs/) can be run as separate [emacs daemon and emacsclient](https://www.emacswiki.org/emacs/EmacsAsDaemon).

This strategy would probably be the most future-safe. Implementing it would be simple, once parti-time has a REST server feature. The CLI would become yet another UI. Right now, it would mean considerable development effort. Also, distribution and deployment gets far more complex, once you deliver your product as multiple separate components.

## Alternative 2: GraalVM Native Image

With the advent of [GraalVM](https://www.graalvm.org/) und the [GraalVM native image addon](https://www.graalvm.org/docs/reference-manual/native-image/), there is now the possibility to compile JVM programs into native binaries. There are some caveats, though:

* [Reflection is not fully supported on Graal's substrate VM](https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md)
* Up to version 1.10.1, Clojure Spec had issues with compiling to native images. Starting with version 1.10.2-alpha1, this is fixed.
* Writing for ahead-of-time compilation means losing some of the dynamic nature and flexibility of Clojure.

For example, none of the popular Clojure wrappers [java-time](https://github.com/dm3/clojure.java-time) and [tick](https://github.com/juxt/tick) for the JSR 310 Java SE 8 Date and Time API worked with GraalVM native image generation. As a workaround I had to roll my own purpose-specific java.time wrapper functions.

Build management is eased up by [lein-native-image](https://github.com/taylorwood/lein-native-image).

A nice extra of creating native images, is the distribution as a single native binary. People are used to this comfort from go projects like [kubernetes](https://kubernetes.io/).
