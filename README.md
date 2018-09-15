# PDI Combination sequence Plugin


## Overview

This plugin provide PDI step that adds a sequence value which resets or increment each time a value changes in the list of specified fields.

![Image](https://github.com/nadment/pdi-combinationsequence-plugin/blob/master/src/main/resources/combinationsequence.png)

## How to install

#### System Requirements

Pentaho Data Integration 8.0 or above

#### Using Pentaho Marketplace

1. Find the plugin in the [Pentaho Marketplace](http://www.pentaho.com/marketplace) and click Install
2. Restart Spoon

#### Manual Install

1. Place the “pdi-combinationsequence-plugin” folder in the ${DI\_HOME}/plugins/ directory
2. Restart Spoon


## Documentation


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

## Support

This plugin for PDI is provided “as is”, without any warranties, expressed or implied. This software is not covered by any Support Agreement.


## License

Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).