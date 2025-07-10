## blankaspect/jsonTree

`jsonTree` is a Java library that contains classes for converting
[JSON](https://datatracker.ietf.org/doc/html/rfc8259) text to and from two data formats that are
similar to JSON:

*  [*Basictree*](#basictree)<br/>
This is a tree of lightweight nodes that are instances of subclasses of an abstract Java base
class, `AbstractNode`.  For want of a better name, the tree is referred to here as a *basictree*
after the simple name of the Java package that contains `AbstractNode` and its subclasses.  The
nodes are equivalent to JSON values except that there are three node types (32-bit integer,
64-bit integer and IEEE-754 double-precision floating point) that correspond to a JSON number.

*  [*JSON-XML*](#json-xml)<br/>
This is an XML-based text format that is defined by two alternative [XSD
schemas](https://en.wikipedia.org/wiki/XML_Schema_(W3C)), one of which has a target namespace.
The elements of JSON-XML are equivalent to JSON values.  JSON-XML is not intended to be a
data-interchange format; it is intended to serve as an intermediate representation in the
transformation of JSON text:<br/>
&nbsp;&nbsp;&nbsp;&nbsp;**JSON<sub>1</sub>**
&nbsp;&rarr;<sup>\(parser\)</sup>&rarr;&nbsp; **JSON-XML<sub>1</sub>**
&nbsp;&rarr;<sup>\(XSLT\)</sup>&rarr;&nbsp; **JSON-XML<sub>2</sub>**
&nbsp;&rarr;<sup>\(generator\)</sup>&rarr;&nbsp; **JSON<sub>2</sub>**

The Java version of the source code is 17.

<div id="basictree">

### Basictree

</div>

The subclasses of `AbstractNode` have the following correspondence with JSON values:

| JSON value      | Node class    | Node state                                      |
|-----------------|---------------|-------------------------------------------------|
| `null`          | `NullNode`    |                                                 |
| `false`, `true` | `BooleanNode` | `boolean`                                       |
| number          | `IntNode`     | `int`                                           |
| number          | `LongNode`,   | `long`                                          |
| number          | `DoubleNode`  | `double`                                        |
| string          | `StringNode`  | `java.lang.String`                              |
| array           | `ListNode`    | `java.util.ArrayList<AbstractNode>`             |
| object          | `MapNode`     | `java.util.LinkedHashMap<String, AbstractNode>` |

A tree of `AbstractNode`s can be traversed with the methods of the
`uk.blankaspect.common.tree.TreeUtils` class.

<div id="json-xml">

### JSON-XML

</div>

JSON-XML is defined by two schemas, `jsonXml.xsd` and `jsonXmlNS.xsd`, which are resources in
the `common.jsonxml` package:
*  `jsonXml.xsd` does not have a target namespace.
*  `jsonXmlNS.xsd` has a target namespace, `http://ns.blankaspect.uk/jsonXml-1`.

It is unlikely that the target namespace will be needed if JSON-XML is used only as an
intermediate representation in the transformation of JSON text.

JSON-XML elements have the following correspondence with JSON values:

| JSON value      | XML element tag | XML element types in schema          |
|-----------------|-----------------|--------------------------------------|
| `null`          | `<null>`        | `JsonNull`<br/>`JsonNullNamed`       |
| `false`, `true` | `<boolean>`     | `JsonBoolean`<br/>`JsonBooleanNamed` |
| number          | `<number>`      | `JsonNumber`<br/>`JsonNumberNamed`   |
| string          | `<string>`      | `JsonString`<br/>`JsonStringNamed`   |
| array           | `<array>`       | `JsonArray`<br/>`JsonArrayNamed`     |
| object          | `<object>`      | `JsonObject`<br/>`JsonObjectNamed`   |

Each kind of JSON-XML element is associated with a pair of related types in the two schemas.
The names of the types are meaningful only within the schemas.

*  If an element is a child of an `<object>` element, it has a `name` attribute and the name of
its type has the suffix `Named`.
*  If an element is not a child of an `<object>` element, it does not have a `name` attribute
and the name of its type does not have the suffix `Named`.

All kinds of JSON-XML element apart from `JsonNull` and `JsonNullNamed` have a `value`
attribute.

JSON text may be transformed by using JSON-XML as an intermediate representation to which an
[XSL transformation](https://en.wikipedia.org/wiki/XSLT) is applied:

1.  Transform JSON<sub>1</sub> to JSON-XML<sub>1</sub> using a [JSON parser](#json-parser).
2.  Transform JSON-XML<sub>1</sub> to JSON-XML<sub>2</sub> using XSLT or another XML processor.
3.  Transform JSON-XML<sub>2</sub> to JSON<sub>2</sub> using a [JSON
generator](#json-generator-json-xml).

<div id="json-parser">

### JSON parser

</div>

The `JsonParser` class transforms JSON text to one of two kinds of rooted tree: either a
[basictree](#basictree) or a tree of [JSON-XML](#json-xml) elements.  When transforming JSON
text to JSON-XML, a parser must be supplied with an implementation of a simple interface,
`IElementFacade`, through which JSON-XML elements are created and their attributes accessed.
The `SimpleElementFacade` class may be suitable for this purpose.

The parser is implemented as a [finite-state
machine](https://en.wikipedia.org/wiki/Finite-state_machine) (FSM) that terminates with an
exception at the first error in the input text.  The FSM combines the lexical analysis and
parsing of the input text with the generation of an output tree (a basictree or a tree of
JSON-XML elements) whose nodes correspond to JSON values.

A parser is instantiated through its builder; for example:

```java
String jsonText = """
{
  "name": "Pont Valentr\u00E9",
  "location": [ 44.44505, 1.43167 ]
}""";
AbstractNode root = JsonParser.builder().build().parse(jsonText);
```

or

```java
String jsonText = """
{
  "name": "Puente de Alc\u00E1ntara",
  "location": [ 39.72246, -6.89253 ]
}""";
Element root = JsonParser.builder()
        .elementFacade(new SimpleElementFacade("json-xml"))
        .build()
        .parseToXml(jsonText);
```

### JSON generators

There are two JSON generators: one transforms a [basictree to JSON
text](#json-generator-basictree), the other transforms [JSON-XML to JSON
text](#json-generator-json-xml).

<div id="json-generator-basictree">

#### Generator 1 : basictree to JSON

</div>

The `JsonGenerator` class transforms a basictree to JSON text.  A generator is instantiated
through its builder, and the root of the basictree is passed to the `generate(AbstractNode)`
method of the generator.  For example,
```java
MapNode bridgeNode = new MapNode();
bridgeNode.addString("name", "Pont Valentr\u00E9");
bridgeNode.addDoubles("location", 44.44505, 1.43167);
String jsonText = JsonGenerator.builder().build().generate(bridgeNode).toString();
```
The form of the generated JSON text is controlled by parameters that are set on the builder (eg,
the `OutputMode` that controls whitespace between the tokens of the JSON text).

<div id="json-generator-json-xml">

#### Generator 2 : JSON-XML to JSON

</div>

The `JsonGeneratorXml` class transforms a tree of JSON-XML elements to JSON text.  A generator
is instantiated through its builder, and the root of the tree of JSON-XML elements is passed to
the `generate(Element)` method of the generator.  For example,

```java
String xmlText = """
    <?xml version="1.0" encoding="UTF-8"?>
    <json-xml>
      <object>
        <string name="name" value="Puente de Alc\u00E1ntara"/>
        <array name="location">
          <number value="39.72246"/>
          <number value="-6.89253"/>
        </array>
      </object>
    </json-xml>""";
Element element = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(new InputSource(new StringReader(xmlText))).getDocumentElement();
String jsonText = JsonGeneratorXml.builder().build()
        .generate(JsonXmlUtils.child(element, 0)).toString();
```

In its traversal of the input tree, the generator ignores all DOM nodes except for the six kinds
of JSON-XML element and their `name` and `value` attributes.

The form of the generated JSON text is controlled by parameters that are set on the builder (eg,
the `OutputMode` that controls whitespace between the tokens of the JSON text).

### Demonstration applications

The `apps` directory contains two command-line programs that demonstrate some aspects of the
`jsonTree` library.  Each program has a Gradle Kotlin-DSL build script that contains tasks to do
the following:
*  compile the program,
*  create an executable JAR,
*  run the main class,
*  run an executable JAR.

#### JsonTransformer

This command-line program demonstrates the use of JSON-XML as an intermediary in the
transformation of JSON text.  It depends on the open-source Java Saxon-HE library from
[Saxonica](https://www.saxonica.com/).

The program generates some *input* JSON text and transforms it indirectly to *output* JSON text
by applying an XSL transformation to the JSON-XML that is equivalent to the input JSON text.
The input JSON text represents a table of the names of Nobel laureates in three *fields* (rows)
over five *years* (columns).  The XSL transformation transposes the rows and columns of the
table so that, in the output JSON text, the years are the rows and the fields are the columns.
The XSL transformation is defined in a stylesheet that requires XSLT 2.0.  The transformation is
performed by the Saxon-HE XSLT 3.0 processor.

If the location of a directory (the *output directory*) is supplied as a command-line argument,
the program's input and output JSON text and JSON-XML documents will be written to the
directory; otherwise, no files will be written.

The program performs the following steps:

 1.  Generate the input table as a basictree (ie, a tree of `AbstractNode`s), omitting a
randomly selected *year* (column) from each *field* (row) &mdash; a different year for each
field.
 2.  Transform the basictree to JSON text with an instance of `JsonGenerator`.
 3.  Optionally write the input JSON text to a file named `nobelLaureates.json` in the output
directory.
 4.  Parse the JSON text with an instance of `JsonParser` to produce the input tree of JSON-XML
elements.
 5.  Optionally create the text of an XML document that contains the input tree of JSON-XML
elements and write the text to a file named `nobelLaureates.xml` in the output directory.
 6.  Apply the XSL transformation, `nobelLaureates.xsl`, to the input tree of JSON-XML elements
to produce the output tree of JSON-XML elements.
 7.  Validate the output tree of JSON-XML elements against the XSD schema, `jsonXml.xsd`.
 8.  Optionally create the text of an XML document that contains the output tree of JSON-XML
elements and write the text to a file named `nobelLaureates-out.xml` in the output
directory.
 9.  Transform the output tree of JSON-XML elements to JSON text with an instance of
`JsonGeneratorXml`.
10.  Optionally write the output JSON text to a file named `nobelLaureates-out.json` in the
output directory.

#### JsonXmlTest

This command-line program demonstrates the use of classes from the `common.jsonxmltest` package
to test the generation and parsing of JSON text via a basictree or a tree of JSON-XML elements,
and to test the copying of a basictree or a tree of JSON-XML elements.  The classes, which were
written to support unit tests, were originally &mdash; and more appropriately &mdash; located in
the`src/test/java` directory of the `common.jsonxml` package, alongside unit tests.  They were
moved to their current location to simplify Gradle build scripts.

The program generates an *input tree* &mdash; either a basictree or a tree of JSON-XML elements
&mdash; and performs two tests on the tree:

*  **Round trip**
    1.  Transform the input tree to JSON text.
    2.  Parse the JSON text to produce an *output tree* of the same kind as the input tree (ie,
        a basictree or a tree of JSON-XML elements).
    3.  Compare the input tree and the output tree.

*  **Copy tree**
    1.  Create a deep copy of the input tree.
    2.  Compare the input tree and the copy.</br>
    *If the test is performed on a tree of JSON-XML elements for which an XML namespace prefix
    was supplied, these additional steps are performed:*
    3.  Create a deep copy of the input tree without the namespace prefix.
    4.  Compare the input tree and the copy, ignoring the namespace prefix of the input tree.

The tests are configured by the program's command-line options, the most important of which is
the `--xml` option.  If the `--xml` option is present, the tests involve a tree of JSON-XML
elements; otherwise, the tests involve a basictree.

Among the program's other command-line options are ones that control the following parameters of
an input tree:
*  The height of a tree (the greatest number of levels of a leaf node below the root).
*  The number of children of a node that corresponds to a JSON array or object.
*  The length of the value of a node that corresponds to a JSON string.
*  The proportions of the different kinds of node.

There is also an option to supply the seed of the pseudo-random number generator that is used to
create the input tree.

The full set of options appears in the program's usage message, which can be displayed by
running the program with the command-line argument `help` or `--help`.

### License

You may use the contents of this repository under the terms of the
[MIT license](https://mit-license.org/).
