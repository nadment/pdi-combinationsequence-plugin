# Overview #

This plugin provide PDI step that adds a sequence value which resets or increment each time a value changes in the list of specified fields.

# System Requirements #

Pentaho Data Integration 8.0 or above

# How to install #

## Using Pentaho Marketplace ##

1. In the [Pentaho Marketplace] (http://www.pentaho.com/marketplace) find the AS400 plugin and click Install
2. Restart Spoon

## Manual Install ##

1. Place the pdi-as400 folder in the ${DI\_HOME}/plugins/ directory
2. Restart Spoon


# Usage #

TODO:

 Sequence mode example
 
 Field1 | Field2 | RESET | INCREMENT
 -------|--------|-------|----------
 A | B | 1 | 1 
 A | B | 2 | 1
 A | B | 3 | 1
 A | C | 1 | 2
 C | C | 1 | 3
 C | C | 2 | 3
 D | C | 1 | 4 

# License #

Licensed under the Apache License, Version 2.0. See LICENSE.txt for more information.