# Java Git Version Tree [![Apache License, Version 2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

`jgvt` is a [git](https://git-scm.com/) version tree GUI viewer with more
of a ClearCase version tree look-n-feel.

It is written in Java using Swing.

# Build

Use the following command to build.  There are convenience development
code that are disguised as tests.  So it is necessary to skip the
tests when building the package.

```bash
	mvn clean package -Dmaven.test.skip=true
```

# License

`jgvt` is provided under [Apache License 2.0]((http://www.apache.org/licenses/LICENSE-2.0)).

Additionally, it uses some libraries with permissive licenses.
 
* [JGit](https://www.eclipse.org/jgit/) - [Eclipse Distribution License 1.0](https://www.eclipse.org/org/documents/edl-v10.php), which is a new-style BSD license.
* [JGraphX](https://github.com/jgraph/jgraphx) - [BSD License](https://github.com/jgraph/jgraphx/blob/master/license.txt).
* [SLF4J](https://www.slf4j.org/) - [MIT License]((https://www.slf4j.org/license.html)).

# Other Similar Tools

* [gitk](https://git-scm.com/docs/gitk)
* [GitVersionTree](https://github.com/crc8/GitVersionTree)
