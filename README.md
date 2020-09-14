### blankaspect/jsonTree

This repository contains the source code of a Java package that provides a parser and a generator for JavaScript Object
Notation \(JSON\).

The parser transforms text that conforms to the JSON grammar into a tree whose nodes are subclasses of the abstract
class `uk.blankaspect.common.basictree.AbstractNode` .

The generator performs the reverse operation, transforming a tree of `AbstractNode`s into JSON text, with some control
over the formatting of the output.

The tree of `AbstractNode`s can be traversed with the methods of the `uk.blankaspect.common.tree.TreeUtils` class.

The Java version of the source code is 1.8 \(Java SE 8\).

All the source files in this repo have a tab width of 4. 

You may use any of the source code under the terms of the MIT license.
