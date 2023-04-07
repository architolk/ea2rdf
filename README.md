# ea2rdf
Reading enterprise architect models and export to ref

## Usage:

```
java -jar ea2rdf.jar [-ea] <option> <filename> [tablename]
```

Filename might be anything, usually .eap or .eapx, but also .mdb files can be read.

| |Options|
|-|-------|
| -t | Show all user tables in the database |
| -s | Show a table: all columns for that particular table (using the table name) |
| -s0 | Show a table: all columns for that particular table (using the table index) |
| -r | Read a table, print all content to standard output |
| -p | Print a table (using the table name), exporting it to RDF (generic approach) |
| -p0 | Print a table (using the table index), exporting it to RDF (generic approach) |
| -e | Export all tables from the database (generic or specific for EA with the -ea option) |

Using the -ea option, the engine will expect an EA database, without the option, a more generic approach is used.

**NOTE**: using the -ea option, by default the encoding is set to Windows encoding! This can be overriden by adding the -0 option directly after the -ea option.

## Dependency:

This application depends on the jackcess library which should be installed using `mvn clean install` from a local copy of the jackcess sources.

Currently, three changes should be made to make jackcess run correctly:
1. Add jacoco plugin to `pom.xml`;
2. Fix the `assertSameDate` routine;
3. Correct the assertion for `testAncientDates`;
4. Disable the assertions for `testReadExtendedDate`;
5. Change the implementation of `readSystemCatalog` so the DefaultTableFinder won't be used, but instead the FallbackTableFinder. This is necessary, because the DefaultTableFinder won't find all the tables for .eap files using version 3 of the Jet engine.

See [https://github.com/architolk/jackcess](https://github.com/architolk/jackcess) for a fork with these changes.
