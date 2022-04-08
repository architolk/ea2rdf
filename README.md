# ea2rdf
Reading enterprise architect models and export to ref

## Usage:

```
java -jar ea2rdf.jar <option> <filename> [tablename]
```

Filename might be anything, usually .eap or .eapx, but also .mdb files can be read.

| |Options|
|-|-------|
| -t | Show all user tables in the database |
| -s | Show a table: all columns for that particular table |
| -r | Read a table, print all content to standard output |
| -p | Print a table, exporting it to RDF (generic approach) |
| -e | Export the database, expecting it to be a EA database (won't work for generic MDB files)

## Dependency:

This application depends on the jackcess library which should be installed using `mvn clean install` from a local copy of the jackcess sources.

Currently, three changes should be made to make jackcess run correctly:
1. Correct the assertion for `testAncientDates`;
2. Disable the assertions for `testReadExtendedDate`;
3. Change the implementation of `readSystemCatalog` so the DefaultTableFinder won't be used, but instead the FallbackTableFinder. This is necessary, because the DefaultTableFinder won't find all the tables for .eap files using version 3 of the Jet engine.
