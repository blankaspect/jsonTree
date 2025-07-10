/*====================================================================*\

JsonXmlTests.java

Class: tests relating to the processing of JSON text and JSON-XML.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.jsonxmltest;

//----------------------------------------------------------------------


// IMPORTS


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.Element;

import uk.blankaspect.common.basictree.AbstractNode;
import uk.blankaspect.common.basictree.BooleanNode;
import uk.blankaspect.common.basictree.DoubleNode;
import uk.blankaspect.common.basictree.IntNode;
import uk.blankaspect.common.basictree.ListNode;
import uk.blankaspect.common.basictree.LongNode;
import uk.blankaspect.common.basictree.MapNode;
import uk.blankaspect.common.basictree.NodeType;
import uk.blankaspect.common.basictree.NullNode;
import uk.blankaspect.common.basictree.StringNode;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;
import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.json.JsonConstants;
import uk.blankaspect.common.json.JsonParser;

import uk.blankaspect.common.jsonxml.ElementKind;
import uk.blankaspect.common.jsonxml.IElementFacade;
import uk.blankaspect.common.jsonxml.JsonGeneratorXml;
import uk.blankaspect.common.jsonxml.JsonXmlUtils;
import uk.blankaspect.common.jsonxml.JsonXmlValidator;
import uk.blankaspect.common.jsonxml.SimpleElementFacade;

import uk.blankaspect.common.text.Tabulator;

import uk.blankaspect.common.treetraversal.TreeTraversal;

//----------------------------------------------------------------------


// CLASS: TESTS RELATING TO THE PROCESSING OF JSON TEXT AND JSON-XML


/**
 * This class implements tests that relate to the processing of JSON text and JSON-XML.
 */

public class JsonXmlTests
	extends TestsBase
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The XML declaration. */
	private static final	String	XML_DECLARATION	= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	/** The name of the document element of a JSON-XML document. */
	private static final	String	DOCUMENT_ELEMENT_NAME	= "json-xml";

	/** The start tag of a JSON-XML document without a namespace attribute. */
	private static final	String	DOCUMENT_START_TAG	= "<" + DOCUMENT_ELEMENT_NAME + ">\n";

	/** The start tag of a JSON-XML document with a namespace attribute. */
	private static final	String	DOCUMENT_START_TAG_NS	=
			"<" + DOCUMENT_ELEMENT_NAME + " xmlns:%s=\"" + JsonXmlValidator.NAMESPACE_NAME + "\">\n";

	/** The end tag of a JSON-XML document. */
	private static final	String	DOCUMENT_END_TAG	= "</" + DOCUMENT_ELEMENT_NAME + ">\n";

	/** Miscellaneous strings. */
	private static final	String	NO_NAMESPACE_STR	= "no namespace";

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The namespace prefix that is applied when creating JSON-XML elements and accessing their attributes. */
	private	String							xmlNamespacePrefix;

	/** The root of the input tree. */
	private	Element							inRoot;

	/** The element facade for the input tree. */
	private	IElementFacade					inElementFacade;

	/** A map of information about the nodes of the input tree. */
	private	Map<ElementKind, NodeKindInfo>	inInfos;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a test of the parsing and generation of JSON text to and from a tree of JSON-XML
	 * elements.
	 */

	public JsonXmlTests()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates an input tree for tests using a pseudo-random number generator that is initialised with the specified
	 * seed.
	 *
	 * @param  seed
	 *           the seed for the pseudo-random number generator.
	 * @param  xmlNamespacePrefix
	 *           the namespace prefix that will be applied to the names of JSON-XML elements and the names of attributes
	 *           of JSON-XML elements when creating elements and accessing their attributes.
	 * @param  treeHeight
	 *           the height of the JSON tree (the length of the longest path from the root of the tree to a leaf node).
	 * @param  numChildrenSource
	 *           the source of the number of children of a generated JSON array or object.
	 * @param  stringLengthSource
	 *           the source of the length of a generated JSON string.
	 * @param  nodeTypeProportions
	 *           a map from types of node to their expected proportions in the generated tree.
	 * @param  allowContainerLeaf
	 *           if {@code true}, a container (JSON array or object) may be a leaf node.
	 * @param  writeInfo
	 *           if {@code true}, information about the input tree will be written to the standard output stream.
	 * @throws BaseException
	 *           if an error occurs when creating an XML document.
	 */
	public void createInputTree(
		long					seed,
		String					xmlNamespacePrefix,
		int						treeHeight,
		N0Source				numChildrenSource,
		N0Source 				stringLengthSource,
		Map<NodeType, Double>	nodeTypeProportions,
		boolean					allowContainerLeaf,
		boolean					writeInfo)
		throws BaseException
	{
		// Update instance variables
		this.xmlNamespacePrefix = xmlNamespacePrefix;

		// Create pseudo-random number generator
		seed &= SEED_MASK;
		prng = new Random(seed);

		// Write seed of PRNG to standard output stream
		if (writeInfo)
		{
			System.out.println(SEED_STR + EQUALS_STR + seed);
			System.out.println(LINE_SEPARATOR_STR);
		}

		// Create element facade for input tree of JSON-XML elements
		inElementFacade = createElementFacade();

		// Generate tree of nodes that correspond to JSON values
		inRoot = generateTree(treeHeight, numChildrenSource, stringLengthSource, nodeTypeProportions,
							  allowContainerLeaf, inElementFacade);

		// Get info about JSON values that correspond to nodes of input tree
		inInfos = getNodeKindInfo(inElementFacade, inRoot);

		// Write information about nodes of input tree to standard output stream
		if (writeInfo)
		{
			System.out.println("[ " + INPUT_TREE_STR + " ]");
			writeNodeKindInfo(inInfos);
			System.out.println(LINE_SEPARATOR_STR);
		}
	}

	//------------------------------------------------------------------

	/**
	 * Tests {@link JsonGeneratorXml} and {@link JsonParser} by performing a round trip that transforms an input tree of
	 * JSON-XML elements to an output tree of JSON-XML elements via JSON text.  The following steps are performed:
	 * <ol>
	 *   <li>
	 *     Generate JSON text from an input tree that was created by {@link #createInputTree(long, String, int,
	 *     N0Source, N0Source, Map, boolean, boolean) createInputTree(&hellip;)}.
	 *   </li>
	 *   <li>
	 *     Parse the JSON text to produce an output tree of JSON-XML elements.
	 *   </li>
	 *   <li>
	 *     Compare the nodes of the input tree and the output tree.
	 *   </li>
	 *   <li>
	 *     Optionally write the result to the standard output stream.
	 *   </li>
	 * </ol>
	 * <p>
	 * This method returns {@code true} if the input tree matches the output tree.
	 * </p>
	 *
	 * @param  printableAsciiOnly
	 *           if {@code true}, the values of JSON strings and the names of the members of JSON objects will be
	 *           escaped so that they contain only printable characters from the US-ASCII character encoding.
	 * @param  useFile
	 *           if {@code true}, the generated JSON text will be written to a temporary file, and it will be parsed by
	 *           opening an input stream on the file; otherwise, the generated JSON text will be parsed directly.
	 * @param  jsonFile
	 *           the location of the file to which the generated JSON text will be written so that it may be inspected.
	 *           If it is {@code null}, the JSON text will not be written to a persistent file.
	 * @param  xmlFile
	 *           the location of the file to which a JSON-XML document containing the generated tree of elements will
	 *           be written so that it may be inspected.  If it is {@code null}, the the generated tree of JSON-XML
	 *           elements will not be written to a persistent file.
	 * @param  validateXml
	 *           if {@code true}, the generated tree of JSON-XML elements will be validated.
	 * @param  writeResult
	 *           if {@code true}, the result of the test will be written to the standard output stream.
	 * @return {@code true} if the test was successful; {@code false} otherwise.
	 * @throws IllegalStateException
	 *           if there is no input tree.
	 * @throws BaseException
	 *           if an error occurs when performing the test.
	 */

	public boolean testRoundTrip(
		boolean	printableAsciiOnly,
		boolean	useFile,
		Path	jsonFile,
		Path	xmlFile,
		boolean	validateXml,
		boolean	writeResult)
		throws BaseException
	{
		// Test for input tree
		if (inRoot == null)
			throw new IllegalStateException(NO_INPUT_TREE_STR);

		// Generate JSON text from input tree
		String jsonText = JsonGeneratorXml.builder()
				.maxLineLength(JSON_MAX_LINE_LENGTH)
				.printableAsciiOnly(printableAsciiOnly)
				.elementFacade(inElementFacade)
				.build()
				.generate(inRoot)
				.toString();

		// Write JSON text to file
		if (jsonFile != null)
		{
			try
			{
				Files.writeString(jsonFile, jsonText);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.ERROR_WRITING_FILE, e, jsonFile);
			}
		}

		// Create element facade for output tree
		IElementFacade outElementFacade = createElementFacade();

		// Initialise root element of output tree
		Element outRoot = null;

		// Case: write JSON text to temporary file; open input stream on file and parse text from input stream
		if (useFile)
		{
			Path file = null;
			try
			{
				// Create temporary directory
				Path directory = createTempDirectory(TEMP_DIRECTORY_NAME);

				// Create output file
				try
				{
					file = Files.createTempFile(directory, getClass().getSimpleName().toLowerCase(),
												JSON_FILENAME_EXTENSION);
				}
				catch (Exception e)
				{
					throw new BaseException(ErrorMsg.FAILED_TO_CREATE_TEMPORARY_FILE, e);
				}

				// Write file
				try
				{
					Files.writeString(file, jsonText);
				}
				catch (Exception e)
				{
					throw new FileException(ErrorMsg.ERROR_WRITING_FILE, e, file);
				}

				// Release reference to JSON text to allow garbage collection
				jsonText = null;

				// Open character stream on file and parse text from stream
				try
				{
					try (BufferedReader reader = Files.newBufferedReader(file))
					{
						outRoot = JsonParser.builder().elementFacade(outElementFacade).build().parseToXml(reader);
					}
				}
				catch (IOException e)
				{
					throw new FileException(ErrorMsg.ERROR_READING_FILE, e, file);
				}
				catch (JsonParser.ParseException e)
				{
					throw new FileException(ErrorMsg.ERROR_PARSING_JSON_TEXT, e, file);
				}
			}
			finally
			{
				// Delete output file
				if (file != null)
				{
					try
					{
						Files.deleteIfExists(file);
					}
					catch (Exception e)
					{
						// ignore
					}
				}
			}
		}

		// Case: parse JSON text
		else
		{
			try
			{
				outRoot = JsonParser.builder().elementFacade(outElementFacade).build().parseToXml(jsonText);
			}
			catch (JsonParser.ParseException e)
			{
				throw new BaseException(ErrorMsg.ERROR_PARSING_JSON_TEXT, e);
			}
		}

		// Write XML document to file
		if (xmlFile != null)
		{
			try (BufferedWriter writer = Files.newBufferedWriter(xmlFile))
			{
				// Write XML declaration
				writer.write(XML_DECLARATION);

				// Write start tag of document element
				writer.write((xmlNamespacePrefix == null)
										? DOCUMENT_START_TAG
										: String.format(DOCUMENT_START_TAG_NS, xmlNamespacePrefix));

				// Write tree of JSON-XML elements
				JsonXmlUtils.writeXml(outRoot, 2, 2, writer);

				// Write end tag of document element
				writer.write(DOCUMENT_END_TAG);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.ERROR_WRITING_FILE, e, xmlFile);
			}
		}

		// Validate JSON-XML
		if (validateXml)
			JsonXmlValidator.validate(outRoot, xmlNamespacePrefix != null);

		// Compare nodes of input tree and output tree
		boolean match = JsonXmlUtils.equals(inElementFacade, inRoot, outElementFacade, outRoot);

		// Write information about nodes of output tree to standard output stream
		if (!match && writeResult)
		{
			System.out.println("[ " + OUTPUT_TREE_STR + " ]");
			writeNodeKindInfo(getNodeKindInfo(outElementFacade, outRoot));
			System.out.println(LINE_SEPARATOR_STR);
		}

		// Write result of test
		if (writeResult)
		{
			System.out.println(ROUND_TRIP_STR + COLON_STR + (match ? MATCH_STR : NO_MATCH_STR));
			System.out.println(LINE_SEPARATOR_STR);
		}

		// Return result
		return match;
	}

	//------------------------------------------------------------------

	/**
	 * Tests the copying of a tree of JSON-XML elements by {@link JsonXmlUtils#copy(IElementFacade, IElementFacade,
	 * Element)}.  The test is performed on the tree that was created by {@link #createInputTree(long, String, int,
	 * N0Source, N0Source, Map, boolean, boolean) createInputTree(&hellip;)}.
	 *
	 * @param  writeResult
	 *           if {@code true}, the result of the test will be written to the standard output stream.
	 * @return {@code true} if the test was successful; {@code false} otherwise.
	 * @throws IllegalStateException
	 *           if there is no input tree.
	 * @throws BaseException
	 *           if an error occurs when performing the test.
	 */

	public boolean testCopyTree(
		boolean	writeResult)
		throws BaseException
	{
		// Test for input tree
		if (inRoot == null)
			throw new IllegalStateException(NO_INPUT_TREE_STR);

		// Create element facade for copy of input tree
		IElementFacade copyElementFacade = createElementFacade();

		// Copy input tree; compare input tree with copy
		boolean match = JsonXmlUtils.equals(inElementFacade, inRoot, copyElementFacade,
											JsonXmlUtils.copy(inElementFacade, copyElementFacade, inRoot));

		// Write result of test
		if (writeResult)
		{
			System.out.println(COPY_TREE_STR + COLON_STR + (match ? MATCH_STR : NO_MATCH_STR));
			System.out.println(LINE_SEPARATOR_STR);
		}

		// Return result
		return match;
	}

	//------------------------------------------------------------------

	/**
	 * Tests the copying of a tree of JSON-XML elements by {@link JsonXmlUtils#copy(IElementFacade, IElementFacade,
	 * Element)} without a namespace prefix.  The test is performed on the tree that was created by {@link
	 * #createInputTree(long, String, int, N0Source, N0Source, Map, boolean, boolean) createInputTree(&hellip;)} only if
	 * it has a namespace prefix.  The namespace prefix of the input tree is ignored when comparing it with the copy.
	 *
	 * @param  writeResult
	 *           if {@code true}, the result of the test will be written to the standard output stream.
	 * @return {@code true} if the test was successful; {@code false} otherwise.
	 * @throws IllegalStateException
	 *           if there is no input tree or the input tree does not have a namespace prefix.
	 * @throws BaseException
	 *           if an error occurs when performing the test.
	 */

	public boolean testCopyTreeIgnoreNS(
		boolean	writeResult)
		throws BaseException
	{
		// Test for input tree
		if (inRoot == null)
			throw new IllegalStateException(NO_INPUT_TREE_STR);

		// Test for namespace prefix
		if (xmlNamespacePrefix == null)
			throw new IllegalStateException("No namespace prefix");

		// Copy input tree without namespace prefix; compare input tree with copy
		Element copyRoot = JsonXmlUtils.copy(inElementFacade, new SimpleElementFacade(DOCUMENT_ELEMENT_NAME), inRoot);
		boolean match = JsonXmlUtils.equalsIgnoreNS(inRoot, copyRoot);

		// Write result of test
		if (writeResult)
		{
			System.out.println(COPY_TREE_STR + ", " + NO_NAMESPACE_STR + COLON_STR
									+ (match ? MATCH_STR : NO_MATCH_STR));
			System.out.println(LINE_SEPARATOR_STR);
		}

		// Return result
		return match;
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a new instance of an {@linkplain IElementFacade element facade} that applies the {@linkplain
	 * #xmlNamespacePrefix namespace prefix} when it creates a JSON-XML element and accesses the attributes of a
	 * JSON-XML element.  When creating an element facade, an XML document will be created to serve as an owner of
	 * elements that are created through the facade.
	 *
	 * @return a new instance of an element facade.
	 * @throws BaseException
	 *           if an error occurs when creating an XML document.
	 */

	private IElementFacade createElementFacade()
		throws BaseException
	{
		return (xmlNamespacePrefix == null)
				? new SimpleElementFacade(DOCUMENT_ELEMENT_NAME)
				: new SimpleElementFacade(JsonXmlValidator.NAMESPACE_NAME, xmlNamespacePrefix, DOCUMENT_ELEMENT_NAME);
	}

	//------------------------------------------------------------------

	/**
	 * Generates a tree of JSON-XML elements with the specified height and returns the tree.  The elements correspond to
	 * JSON values.  The kind of each element and the values of simple element (Boolean, number or string) are chosen
	 * randomly.  The number of children of an array element or object element and the length the value of a string
	 * element are obtained from the specified sources.  The probability of each kind of element is specified by a map
	 * whose values are not normalised (ie, their sum is not necessarily 1).
	 *
	 * @param  treeHeight
	 *           the height of the generated tree.
	 * @param  numChildrenSource
	 *           the source of the number of children of an array element or object element.
	 * @param  stringLengthSource
	 *           the source of the length of a generated value of a string element.
	 * @param  nodeTypeProportions
	 *           a map from types of node to their expected proportions in the generated tree.
	 * @param  allowContainerLeaf
	 *           if {@code true}, an array element or object element may be a leaf node.
	 * @return the root of the tree of JSON-XML elements that correspond to JSON values.
	 */

	private Element generateTree(
		int						treeHeight,
		N0Source				numChildrenSource,
		N0Source 				stringLengthSource,
		Map<NodeType, Double>	nodeTypeProportions,
		boolean					allowContainerLeaf,
		IElementFacade			elementFacade)
	{
		// Initialise the root of the tree
		Element root = null;

		// Initialise map from collections of node types to their CDFs
		Map<List<NodeType>, NodeTypeProbability[]> nodeTypeCdfs = new HashMap<>();

		// Initialise the list of parent elements
		List<Element> parents = new ArrayList<>();
		parents.add(null);

		// Declare record for child element
		record Child(
			Element	parent,
			Element	element)
		{ }

		// Initialise the list of child elements
		List<Child> children = new ArrayList<>();

		// Generate elements at all required levels
		for (int level = 0; level <= treeHeight; level++)
		{
			// Clear the list of children
			children.clear();

			// Generate children for current parents
			int numContainers = 0;
			for (int i = 0; i < parents.size(); i++)
			{
				Element parent = parents.get(i);
				int numChildren = (parent == null) ? 1 : numChildrenSource.next(prng);
				for (int j = 0; j < numChildren; j++)
				{
					List<NodeType> nodeTypes = ((level < treeHeight) || allowContainerLeaf)
														? JsonConstants.NODE_TYPES
														: JsonConstants.SIMPLE_NODE_TYPES;
					Element child = generateElement(nodeTypes, nodeTypeProportions, nodeTypeCdfs, stringLengthSource,
													elementFacade);
					if (ElementKind.isCompound(child))
						++numContainers;
					children.add(new Child(parent, child));
				}
			}

			// If this is not the last level AND there are children AND none of the children is a container, replace a
			// randomly selected child with a container
			if ((level < treeHeight) && !children.isEmpty() && (numContainers == 0))
			{
				Element child = generateElement(JsonConstants.COMPOUND_NODE_TYPES, nodeTypeProportions, nodeTypeCdfs,
												stringLengthSource, elementFacade);
				int index = prng.nextInt(children.size());
				children.set(index, new Child(children.get(index).parent, child));
			}

			// Add the children to their parents
			for (Child child : children)
			{
				// Get the parent of the child
				Element parent = child.parent;

				// If the child has no parent, it is the root of the tree ...
				if (parent == null)
					root = child.element;

				// ... otherwise, if the parent is a container, add the child to its parent
				else if (ElementKind.isCompound(parent))
				{
					// List node
					if (ElementKind.ARRAY.matches(parent))
						parent.appendChild(child.element);

					// Map node
					else if (ElementKind.OBJECT.matches(parent))
					{
						// Add the child as a KV pair of the map node
						while (true)
						{
							// Generate a random key
							String key = generateString(stringLengthSource.next(prng));

							// If the parent doesn't have a KV pair with the key, add the child
							if (JsonXmlUtils.children(parent).stream()
									.noneMatch(child0 -> key.equals(JsonXmlUtils.getName(child0))))
							{
								JsonXmlUtils.setName(elementFacade, child.element, key);
								parent.appendChild(child.element);
								break;
							}
						}
					}
				}

				// ... otherwise, throw an exception
				else
					throw new UnexpectedRuntimeException();
			}

			// Update the list of parents
			parents.clear();
			for (Child child : children)
			{
				if (ElementKind.isCompound(child.element))
					parents.add(child.element);
			}
		}

		// Return the root of the tree
		return root;
	}

	//------------------------------------------------------------------

	/**
	 * Generates and returns a JSON-XML element of a kind that corresponds to a type of {@link AbstractNode} that is
	 * chosen randomly from the specified set.
	 *
	 * @param  nodeTypes
	 *           the types of node from which the kind of generated element will be chosen.
	 * @param  nodeTypeProportions
	 *           a map from types of node to their expected proportions in the generated tree.
	 * @param  nodeTypeCdfs
	 *           a map from collections of types of node to cumulative distribution functions that have been created
	 *           from {@code nodeTypeProportions}.  If the map does not contain an entry for {@code nodeTypes}, one will
	 *           be created and added to the map.
	 * @param  stringLengthSource
	 *           the source of the length of a generated JSON string.
	 * @param  elementFacade
	 *           the facade through which the JSON-XML element will be created and its attributes accessed.
	 * @return a JSON-XML element of a kind that corresponds to a type of node that is chosen randomly from {@code
	 *         nodeTypes}.
	 */

	private Element generateElement(
		List<NodeType>								nodeTypes,
		Map<NodeType, Double>						nodeTypeProportions,
		Map<List<NodeType>, NodeTypeProbability[]>	nodeTypeCdfs,
		N0Source 									stringLengthSource,
		IElementFacade								elementFacade)
	{
		NodeTypeProbability[] cdf = nodeTypeCdf(nodeTypes, nodeTypeProportions, nodeTypeCdfs);

		double selector = prng.nextDouble();
		for (int i = 0; i < cdf.length; i++)
		{
			if (selector < cdf[i].probability)
			{
				// Initialise element
				Element element = null;

				// Get type of node
				NodeType nodeType = cdf[i].nodeType;

				// Case: null
				if (nodeType == NullNode.TYPE)
					element = ElementKind.NULL.createElement(elementFacade);

				// Case: Boolean
				else if (nodeType == BooleanNode.TYPE)
				{
					element = ElementKind.BOOLEAN.createElement(elementFacade);
					JsonXmlUtils.setValue(elementFacade, element,
										  prng.nextBoolean() ? BooleanNode.VALUE_TRUE : BooleanNode.VALUE_FALSE);
				}

				// Case: int
				else if (nodeType == IntNode.TYPE)
				{
					element = ElementKind.NUMBER.createElement(elementFacade);
					JsonXmlUtils.setValue(elementFacade, element, Integer.toString(prng.nextInt()));
				}

				// Case: long
				else if (nodeType == LongNode.TYPE)
				{
					element = ElementKind.NUMBER.createElement(elementFacade);
					JsonXmlUtils.setValue(elementFacade, element, Long.toString(prng.nextLong()));
				}

				// Case: double
				else if (nodeType == DoubleNode.TYPE)
				{
					element = ElementKind.NUMBER.createElement(elementFacade);
					JsonXmlUtils.setValue(elementFacade, element, Double.toString(generateDouble()));
				}

				// Case: string
				else if (nodeType == StringNode.TYPE)
				{
					element = ElementKind.STRING.createElement(elementFacade);
					JsonXmlUtils.setValue(elementFacade, element, generateString(stringLengthSource.next(prng)));
				}

				// Case: array
				else if (nodeType == ListNode.TYPE)
					element = ElementKind.ARRAY.createElement(elementFacade);

				// Case: object
				else if (nodeType == MapNode.TYPE)
					element = ElementKind.OBJECT.createElement(elementFacade);

				// Return element
				return element;
			}
		}
		throw new UnexpectedRuntimeException();
	}

	//------------------------------------------------------------------

	/**
	 * Traverses the tree of JSON-XML elements whose root is the specified node and returns a map of {@linkplain
	 * NodeKindInfo information} about each {@linkplain ElementKind kind of element}.
	 *
	 * @param  root
	 *           the root of the tree of JSON-XML elements about which information is sought.
	 * @return a map of information about each kind of element in the tree whose root is {@code root}.
	 */

	private Map<ElementKind, NodeKindInfo> getNodeKindInfo(
		IElementFacade	elementFacade,
		Element			root)
	{
		// Initialise map of information about kinds of JSON-XML elements
		Map<ElementKind, NodeKindInfo> infos = new EnumMap<>(ElementKind.class);
		for (ElementKind nodeType : ElementKind.values())
			infos.put(nodeType, new NodeKindInfo());

		// Traverse tree of JSON-XML elements to update information
		TreeTraversal.visitDepthFirst(root, true, true, JsonXmlUtils::children, element ->
		{
			NodeKindInfo info = infos.get(ElementKind.of(element));
			++info.count;
			info.updateMaxDepth(TreeTraversal.getDepth(element, JsonXmlUtils::parent));
			return true;
		});

		// Return map of information
		return infos;
	}

	//------------------------------------------------------------------

	/**
	 * Writes the specified information about the nodes of a tree of JSON-XML elements to the standard output stream.
	 *
	 * @param infos
	 *          a map of information about the nodes of a tree of JSON-XML elements.
	 */

	private void writeNodeKindInfo(
		Map<ElementKind, NodeKindInfo>	infos)
	{
		// Count all nodes of tree
		long elementCount = 0;
		int maxDepth = 0;
		for (ElementKind elementKind : inInfos.keySet())
		{
			NodeKindInfo info = inInfos.get(elementKind);
			elementCount += info.count;
			maxDepth = Math.max(maxDepth, info.maxDepth);
		}
		double countFactor = (elementCount == 0) ? 0.0 : 1.0 / (double)elementCount;

		// Write tree height to standard output stream
		System.out.println(HEIGHT_STR + EQUALS_STR + maxDepth);

		// Tabulate info for each kind of element and write table to standard output stream
		List<String[]> rows = new ArrayList<>();
		for (ElementKind elementKind : infos.keySet())
		{
			NodeKindInfo info = infos.get(elementKind);
			rows.add(new String[]
			{
				"<" + elementKind.key() + ">",
				":",
				INTEGER_FORMATTER.format(info.count),
				"(" + FREQ_FORMATTER.format((double)info.count * countFactor) + ")"
			});
		}
		rows.add(new String[] { TOTAL_STR, ":", INTEGER_FORMATTER.format(elementCount) } );
		int numColumns = rows.stream().mapToInt(row -> row.length).max().orElse(1);
		boolean[] rightAligned = new boolean[numColumns];
		Arrays.fill(rightAligned, true);
		System.out.print(Tabulator.tabulate(numColumns, rightAligned, null, rows).text());
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
