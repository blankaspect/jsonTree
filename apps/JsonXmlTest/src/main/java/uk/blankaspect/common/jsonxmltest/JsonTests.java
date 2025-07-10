/*====================================================================*\

JsonTests.java

Class: tests relating to the processing of JSON text.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.jsonxmltest;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import uk.blankaspect.common.json.JsonGenerator;
import uk.blankaspect.common.json.JsonParser;
import uk.blankaspect.common.json.JsonUtils;

import uk.blankaspect.common.text.Tabulator;

import uk.blankaspect.common.tree.TreeUtils;

//----------------------------------------------------------------------


// CLASS: TESTS RELATING TO THE PROCESSING OF JSON TEXT


/**
 * This class implements tests that relate to the processing of JSON text.
 */

public class JsonTests
	extends TestsBase
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The root of the input tree. */
	private	AbstractNode				inRoot;

	/** A map of information about the nodes of the input tree. */
	private	Map<NodeType, NodeKindInfo>	inInfos;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a test of the parsing and generation of JSON text to and from a tree of subclasses of
	 * {@link AbstractNode}.
	 */

	public JsonTests()
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
	 * @param seed
	 *          the seed for the pseudo-random number generator.
	 * @param treeHeight
	 *          the height of the JSON tree (the length of the longest path from the root of the tree to a leaf node).
	 * @param numChildrenSource
	 *          the source of the number of children of a generated JSON array or object.
	 * @param stringLengthSource
	 *          the source of the length of a generated JSON string.
	 * @param nodeTypeProportions
	 *          a map from types of node to their expected proportions in the generated tree.
	 * @param allowContainerLeaf
	 *          if {@code true}, a container (JSON array or object) may be a leaf node.
	 * @param writeInfo
	 *          if {@code true}, information about the input tree will be written to the standard output stream.
	 */
	public void createInputTree(
		long					seed,
		int						treeHeight,
		N0Source				numChildrenSource,
		N0Source 				stringLengthSource,
		Map<NodeType, Double>	nodeTypeProportions,
		boolean					allowContainerLeaf,
		boolean					writeInfo)
	{
		// Create pseudo-random number generator
		seed &= SEED_MASK;
		prng = new Random(seed);

		// Write seed to standard output stream
		if (writeInfo)
		{
			System.out.println(SEED_STR + EQUALS_STR + seed);
			System.out.println(LINE_SEPARATOR_STR);
		}

		// Generate tree of nodes that correspond to JSON values
		inRoot = generateTree(treeHeight, numChildrenSource, stringLengthSource, nodeTypeProportions,
							  allowContainerLeaf);

		// Get info about JSON values that correspond to nodes of input tree
		inInfos = getNodeKindInfo(inRoot);

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
	 * Tests {@link JsonGenerator} and {@link JsonParser} by performing a round trip that transforms an input tree of
	 * {@link AbstractNode}s to an output tree of {@link AbstractNode}s via JSON text.  The following steps are
	 * performed:
	 * <ol>
	 *   <li>
	 *     Generate JSON text from an input tree that was created by {@link #createInputTree(long, int, N0Source,
	 *     N0Source, Map, boolean, boolean) createInputTree(&hellip;)}.
	 *   </li>
	 *   <li>
	 *     Parse the JSON text to produce an output tree of {@link AbstractNode}s.
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
		boolean	writeResult)
		throws BaseException
	{
		// Test for input tree
		if (inRoot == null)
			throw new IllegalStateException(NO_INPUT_TREE_STR);

		// Generate JSON text from input tree
		String jsonText = JsonGenerator.builder()
				.maxLineLength(JSON_MAX_LINE_LENGTH)
				.printableAsciiOnly(printableAsciiOnly)
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

		// Initialise root node of output tree
		AbstractNode outRoot = null;

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
					outRoot = JsonUtils.readFile(file);
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
				outRoot = JsonParser.builder().build().parse(jsonText);
			}
			catch (JsonParser.ParseException e)
			{
				throw new BaseException(ErrorMsg.ERROR_PARSING_JSON_TEXT, e);
			}
		}

		// Compare nodes of input tree and output tree
		boolean match = inRoot.equals(outRoot);

		// Write information about nodes of output tree to standard output stream
		if (!match && writeResult)
		{
			System.out.println("[ " + OUTPUT_TREE_STR + " ]");
			writeNodeKindInfo(getNodeKindInfo(outRoot));
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
	 * Tests the copying of a tree of {@link AbstractNode}s by {@link AbstractNode#clone()}.  The test is performed on
	 * the tree that was created by {@link #createInputTree(long, int, N0Source, N0Source, Map, boolean, boolean)
	 * createInputTree(&hellip;)}.
	 *
	 * @param  writeResult
	 *           if {@code true}, the result of the test will be written to the standard output stream.
	 * @return {@code true} if the test was successful; {@code false} otherwise.
	 * @throws IllegalStateException
	 *           if there is no input tree.
	 */

	public boolean testCopyTree(
		boolean	writeResult)
	{
		// Test for input tree
		if (inRoot == null)
			throw new IllegalStateException(NO_INPUT_TREE_STR);

		// Copy input tree; compare input tree with copy
		boolean match = inRoot.equals(inRoot.clone());

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
	 * Generates a tree of {@linkplain AbstractNode nodes} with the specified height and returns the tree.  The nodes
	 * correspond to JSON values.  The type of each node and the values of simple nodes (Boolean, number or string) are
	 * chosen randomly.  The number of children of a container node and the length the value of a string node are
	 * obtained from the specified sources.  The probability of each type of node is specified by a map whose values are
	 * not normalised (ie, their sum is not necessarily 1).
	 *
	 * @param  treeHeight
	 *           the height of the generated tree.
	 * @param  numChildrenSource
	 *           the source of the number of children of a container node, which corresponds to a JSON array or object.
	 * @param  stringLengthSource
	 *           the source of the length of a generated value of a string node.
	 * @param  nodeTypeProportions
	 *           a map from types of node to their expected proportions in the generated tree.
	 * @param  allowContainerLeaf
	 *           if {@code true}, a container node (corresponding to a JSON array or object) may be a leaf node.
	 * @return the root of the tree of nodes that correspond to JSON values.
	 */

	private AbstractNode generateTree(
		int						treeHeight,
		N0Source				numChildrenSource,
		N0Source 				stringLengthSource,
		Map<NodeType, Double>	nodeTypeProportions,
		boolean					allowContainerLeaf)
	{
		// Initialise the root of the tree
		AbstractNode root = null;

		// Initialise map from collections of node types to their CDFs
		Map<List<NodeType>, NodeTypeProbability[]> nodeTypeCdfs = new HashMap<>();

		// Initialise the list of parent values
		List<AbstractNode> parents = new ArrayList<>();
		parents.add(null);

		// Initialise the list of child values
		List<AbstractNode> children = new ArrayList<>();

		// Generate values at all required levels
		for (int level = 0; level <= treeHeight; level++)
		{
			// Clear the list of children
			children.clear();

			// Generate children for current parents
			int numContainers = 0;
			for (int i = 0; i < parents.size(); i++)
			{
				AbstractNode parent = parents.get(i);
				int numChildren = (parent == null) ? 1 : numChildrenSource.next(prng);
				for (int j = 0; j < numChildren; j++)
				{
					List<NodeType> nodeTypes = ((level < treeHeight) || allowContainerLeaf)
														? JsonConstants.NODE_TYPES
														: JsonConstants.SIMPLE_NODE_TYPES;
					AbstractNode child = generateNode(nodeTypes, nodeTypeProportions, nodeTypeCdfs, stringLengthSource);
					child.setParent(parent);
					if (child.isContainer())
						++numContainers;
					children.add(child);
				}
			}

			// If this is not the last level AND there are children AND none of the children is a container, replace a
			// randomly selected child with a container
			if ((level < treeHeight) && !children.isEmpty() && (numContainers == 0))
			{
				AbstractNode child = generateNode(JsonConstants.COMPOUND_NODE_TYPES, nodeTypeProportions, nodeTypeCdfs,
												  stringLengthSource);
				int index = prng.nextInt(children.size());
				child.setParent(children.get(index).getParent());
				children.set(index, child);
			}

			// Add the children to their parents
			for (AbstractNode child : children)
			{
				// Get the parent of the child
				AbstractNode parent = child.getParent();

				// If the child has no parent, it is the root of the tree ...
				if (parent == null)
					root = child;

				// ... otherwise, if the parent is a container, add the child to its parent
				else if (parent.isContainer())
				{
					// Get node type
					NodeType nodeType = parent.getType();

					// List node
					if (nodeType == ListNode.TYPE)
					{
						// Add the child as an element of the list node
						((ListNode)parent).add(child);
					}

					// Map node
					else if (nodeType == MapNode.TYPE)
					{
						// Cast the parent to a map node
						MapNode mapNode = (MapNode)parent;

						// Add the child as a KV pair of the map node
						String key = null;
						while (true)
						{
							// Generate a random key
							key = generateString(stringLengthSource.next(prng));

							// If the parent doesn't have a KV pair with the key, add the child
							if (!mapNode.hasKey(key))
							{
								mapNode.add(key, child);
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
			for (AbstractNode child : children)
			{
				if (child.isContainer())
					parents.add(child);
			}
		}

		// Return the root of the tree
		return root;
	}

	//------------------------------------------------------------------

	/**
	 * Generates and returns a {@linkplain AbstractNode node} whose type is chosen randomly from the specified set.
	 *
	 * @param  nodeTypes
	 *           the types of node from which the generated node will be chosen.
	 * @param  nodeTypeProportions
	 *           a map from types of node to their expected proportions in the generated tree.
	 * @param  nodeTypeCdfs
	 *           a map from collections of types of node to cumulative distribution functions that have been created
	 *           from {@code nodeTypeProportions}.  If the map does not contain an entry for {@code nodeTypes}, one will
	 *           be created and added to the map.
	 * @param  stringLengthSource
	 *           the source of the length of a generated JSON string.
	 * @return a node whose type is chosen randomly from {@code nodeTypes}.
	 */

	private AbstractNode generateNode(
		List<NodeType>								nodeTypes,
		Map<NodeType, Double>						nodeTypeProportions,
		Map<List<NodeType>, NodeTypeProbability[]>	nodeTypeCdfs,
		N0Source 									stringLengthSource)
	{
		NodeTypeProbability[] cdf = nodeTypeCdf(nodeTypes, nodeTypeProportions, nodeTypeCdfs);

		double selector = prng.nextDouble();
		for (int i = 0; i < cdf.length; i++)
		{
			if (selector < cdf[i].probability)
			{
				// Initialise node
				AbstractNode node = null;

				// Get type of node
				NodeType nodeType = cdf[i].nodeType;

				// Case: null
				if (nodeType == NullNode.TYPE)
					node = new NullNode();

				// Case: Boolean
				else if (nodeType == BooleanNode.TYPE)
					node = new BooleanNode(prng.nextBoolean());

				// Case: int
				else if (nodeType == IntNode.TYPE)
					node = new IntNode(prng.nextInt());

				// Case: long
				else if (nodeType == LongNode.TYPE)
					node = new LongNode(prng.nextLong());

				// Case: double
				else if (nodeType == DoubleNode.TYPE)
					node = new DoubleNode(generateDouble());

				// Case: string
				else if (nodeType == StringNode.TYPE)
					node = new StringNode(generateString(stringLengthSource.next(prng)));

				// Case: array
				else if (nodeType == ListNode.TYPE)
					node = new ListNode();

				// Case: object
				else if (nodeType == MapNode.TYPE)
					node = new MapNode();

				// Return node
				return node;
			}
		}
		throw new UnexpectedRuntimeException();
	}

	//------------------------------------------------------------------

	/**
	 * Traverses the tree of {@linkplain AbstractNode nodes} whose root is the specified node and returns a map of
	 * {@linkplain NodeKindInfo information} about each {@linkplain NodeType type of node}.
	 *
	 * @param  root
	 *           the root of the tree of nodes about which information is sought.
	 * @return a map of information about each type of node in the tree whose root is {@code root}.
	 */

	private Map<NodeType, NodeKindInfo> getNodeKindInfo(
		AbstractNode	root)
	{
		// Initialise map of JSON-value information
		Map<NodeType, NodeKindInfo> infos = new LinkedHashMap<>();
		for (NodeType nodeType : JsonConstants.NODE_TYPES)
			infos.put(nodeType, new NodeKindInfo());

		// Traverse tree of nodes to update JSON-value information
		TreeUtils.visitDepthFirst(root, true, true, node ->
		{
			NodeKindInfo info = infos.get(node.getType());
			++info.count;
			info.updateMaxDepth(TreeUtils.getDepth(node));
			return true;
		});

		// Return map of JSON-value information
		return infos;
	}

	//------------------------------------------------------------------

	/**
	 * Writes the specified information about the {@linkplain AbstractNode nodes} of a tree to the standard output
	 * stream.
	 *
	 * @param infos
	 *          a map of information about the JSON values that correspond to the {@linkplain AbstractNode nodes} of a
	 *          tree.
	 */

	private void writeNodeKindInfo(
		Map<NodeType, NodeKindInfo>	infos)
	{
		// Count all nodes in tree
		long nodeCount = 0;
		int maxDepth = 0;
		for (NodeType nodeType : infos.keySet())
		{
			NodeKindInfo info = infos.get(nodeType);
			nodeCount += info.count;
			maxDepth = Math.max(maxDepth, info.maxDepth);
		}
		double countFactor = (nodeCount == 0) ? 0.0 : 1.0 / (double)nodeCount;

		// Write tree height to standard output stream
		System.out.println(HEIGHT_STR + EQUALS_STR + maxDepth);

		// Tabulate info for each kind of node and write table to standard output stream
		List<String[]> rows = new ArrayList<>();
		for (NodeType nodeType : infos.keySet())
		{
			NodeKindInfo info = infos.get(nodeType);
			rows.add(new String[]
			{
				nodeType.getNodeClass().getSimpleName(),
				":",
				INTEGER_FORMATTER.format(info.count),
				"(" + FREQ_FORMATTER.format((double)info.count * countFactor) + ")"
			});
		}
		rows.add(new String[] { TOTAL_STR, ":", INTEGER_FORMATTER.format(nodeCount) } );
		int numColumns = rows.stream().mapToInt(row -> row.length).max().orElse(1);
		boolean[] rightAligned = new boolean[numColumns];
		Arrays.fill(rightAligned, true);
		System.out.print(Tabulator.tabulate(numColumns, rightAligned, null, rows).text());
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
