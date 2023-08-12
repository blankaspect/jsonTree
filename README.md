### blankaspect/jsonTree

This repository contains the source code of a Java library that can be used to convert between the representation of
some data as JavaScript Object Notation \(JSON\) text and the representation of the data as a rooted tree.

The library includes a parser and a generator for JSON.  The parser transforms text that conforms to the JSON grammar
into a tree whose nodes are subclasses of the abstract class `uk.blankaspect.common.basictree.AbstractNode` .  The
generator performs the reverse operation, transforming a tree of `AbstractNode`s into JSON text, with some control over
the formatting of the output.

A tree of `AbstractNode`s can be traversed with the methods of the `uk.blankaspect.common.tree.TreeUtils` class.

The Java version of the source code is 17.

The source files in this repo have an expected tab width of 4.

----

You may use the contents of this repository under the terms of the MIT license.
