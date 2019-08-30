Build
=====

**jgvt** requries JDK8+ and `Maven <https://maven.apache.org/>`__ to build.


The following is the build command.  There are convenience launch codes that
are disguised as tests. So it is necessary to skip the tests when building the
package.

.. code-block:: shell

	mvn clean package -Dmaven.test.skip=true
