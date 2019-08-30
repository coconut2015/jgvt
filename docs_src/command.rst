Command Line
============

**jgvt** requries JRE8+ to run.  The typical usage is just run the jgvt
jar in the git repo directory.

In general, the default memory setting is good enough.  jgvt can compute
and display a 10K-commit repo tree without requiring additional memory.

Command Line Options
--------------------

The options are mostly for debugging purposes.

.. code-block:: text

	usage: java -jar jgvt.jar [options] [repo directory]
	 -b,--branch   list branches
	 -d,--debug    print debug messages
	 -h,--help     print this message
	 -s <arg>      specify the last commit of the main branch
	 -t,--tag      list tags

If the repo directory is not specified, the current directory is used.

Shell Script Launcher
---------------------

.. code-block:: bash

	#!/bin/sh
	exec java -jar jgvt-1.0.0.jar $@ &

Windows Batch Command
---------------------

.. code-block:: batch

	@echo off
	start /b javaw -jar jgvt-1.0.0.jar %*
