# liquibase-ext-maven-plugin

This plugin is developed to help developers write liquibase scripts that are compatible with the most commonly used databases such as PostgreSQL, Oracle and IBM DB2.
This plugin is also able to check (at compile time) if the scripts in the project are backwards compatible with respect to the previous (following semantic versioning) released version.

## validate
This goal thanks to a rules engine, which can be disabled individually, check if in the scripts:
* an allowable subset of changesets have been used (e.g. no insert, no delete, ...)
* the types indicated in the changesets are restricted to those defined in SQL99 as a greater guarantee that the same changeset works in different databases without the help of the dbms attribute to change behavior according to the destination DB.
* specific checks depending on the database (supported) avoiding that in production a particular changeset does not work because the maximum number of characters allowed for a column or index has been exceeded or that an index is being deleted without first deleting a costraint applied to it etc. etc

## merge-changelogs
This goal creates a master changelog database that includes all (configurable) liquibase scripts found in the maven project resources and in the build classpath following an order in the reverse dependency hierarchy. For example, if a project A creates a table and another project B extends it, assuming that project B depends on A, the master liquibase script of A will be executed first in the generated file and then that of B. (The scripts must be included in the build / jar classpath).

## updateSQL
This goal uses merge-changelogs to create a master changelog and run it in a configured database (normally H2). It is also possible to enable a backwards compatibility check that allows, before running the current liquid base scripts, to create those extracted from a previous version that respects the semantic versioning.

## Thanks to
Thanks to:

* Laura Cameran

to collaboration to support this project