/*====================================================================*\

JsonXmlTestApp.java

Class: test application for JSON and JSON-XML.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.jsonxmltest;

//----------------------------------------------------------------------


// IMPORTS


import java.nio.file.Path;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.blankaspect.common.basictree.BooleanNode;
import uk.blankaspect.common.basictree.DoubleNode;
import uk.blankaspect.common.basictree.IntNode;
import uk.blankaspect.common.basictree.ListNode;
import uk.blankaspect.common.basictree.LongNode;
import uk.blankaspect.common.basictree.MapNode;
import uk.blankaspect.common.basictree.NodeType;
import uk.blankaspect.common.basictree.NullNode;
import uk.blankaspect.common.basictree.StringNode;

import uk.blankaspect.common.build.BuildUtils;

import uk.blankaspect.common.commandline.CommandLine;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.ExceptionUtils;
import uk.blankaspect.common.exception2.LocationException;
import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.json.JsonConstants;

import uk.blankaspect.common.jsonxmltest.JsonTests;
import uk.blankaspect.common.jsonxmltest.JsonXmlTests;
import uk.blankaspect.common.jsonxmltest.N0Source;

import uk.blankaspect.common.resource.ResourceProperties;
import uk.blankaspect.common.resource.ResourceUtils;

import uk.blankaspect.common.string.StringUtils;

//----------------------------------------------------------------------


// CLASS: TEST APPLICATION FOR JSON AND JSON-XML


public class JsonXmlTestApp
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The single instance of the application. */
	public static final		JsonXmlTestApp	INSTANCE	= new JsonXmlTestApp();

	/** The short name of the application. */
	public static final		String	SHORT_NAME	= "JsonXmlTest";

	/** The long name of the application. */
	public static final		String	LONG_NAME	= "JSON-XML test";

	/** The name of the application when used as a key. */
	public static final		String	NAME_KEY	= StringUtils.firstCharToLowerCase(SHORT_NAME);

	/** The name of the build-properties file. */
	private static final	String	BUILD_PROPERTIES_FILENAME	= "build.properties";

	/** The prefix of a cause in the string representation of an exception. */
	private static final	String	EXCEPTION_CAUSE_PREFIX	= "- ";

	/** The minimum height of a JSON tree. */
	private static final	int		MIN_TREE_HEIGHT		= 0;

	/** The maximum height of a JSON tree. */
	private static final	int		MAX_TREE_HEIGHT		= 500;

	/** The default height of a JSON tree. */
	private static final	int		DEFAULT_TREE_HEIGHT	= 8;

	/** The separator between fields of a source of natural numbers. */
	private static final	char	N0SOURCE_SEPARATOR	= ',';

	/** The minimum number of children of a generated JSON array or object. */
	private static final	int		MIN_NUM_CHILDREN	= 1;

	/** The maximum number of children of a generated JSON array or object. */
	private static final	int		MAX_NUM_CHILDREN	= 40;

	/** The minimum value of the <i>lambda</i> parameter of a Poisson or zero-truncated Poisson distribution from which
		the number of children of a generated JSON array or object is taken. */
	private static final	int		MIN_NUM_CHILDREN_LAMBDA	= 1;

	/** The maximum value of the <i>lambda</i> parameter of a Poisson or zero-truncated Poisson distribution from which
		the number of children of a generated JSON array or object is taken. */
	private static final	int		MAX_NUM_CHILDREN_LAMBDA	= 10;

	/** The default source of the number of children of a generated JSON array or object. */
	private static final	N0Source	DEFAULT_NUM_CHILDREN_SOURCE	= new N0Source.Poisson(5);

	/** The minimum length of a generated JSON string. */
	private static final	int		MIN_STRING_LENGTH	= 0;

	/** The maximum length of a generated JSON string. */
	private static final	int		MAX_STRING_LENGTH	= 80;

	/** The minimum value of the <i>lambda</i> parameter of a Poisson or zero-truncated Poisson distribution from which
		the length of a generated JSON string is taken. */
	private static final	int		MIN_STRING_LENGTH_LAMBDA	= 0;

	/** The maximum value of the <i>lambda</i> parameter of a Poisson or zero-truncated Poisson distribution from which
		the length of a generated JSON string is taken. */
	private static final	int		MAX_STRING_LENGTH_LAMBDA	= 40;

	/** The default source of the length of a generated JSON string. */
	private static final	N0Source	DEFAULT_STRING_LENGTH_SOURCE	= new N0Source.ZtPoisson(8);

	/** A map from types of JSON values to their default proportions in a generated JSON tree. */
	private static final	Map<NodeType, Double>	DEFAULT_NODE_TYPE_PROPORTIONS;

	/** The usage message. */
	private static final	String	USAGE_MESSAGE	=
		"Usage: " + NAME_KEY + " [subcommand] [options]\n\n" +
		"""
		Subcommands:
		  (The '--' prefix of a subcommand may be omitted.)
		  --help
		      Write help information to the standard output stream.
		  --run-tests
		     Run the tests.  This is the default subcommand.
		  --version
		      Write version information to the standard output stream.

		Options:
		  --allow-container-leaf
		      Allow a container node (a node that corresponds to a JSON array or
		      object) to be a leaf node.
		  --json-output=<pathname>
		      Write the generated JSON text to a file at the specified location.
		  --num-children=fixed,<value>
		  --num-children=uniform,<lower-bound>,<upper-bound>
		  --num-children=poisson,<lambda>
		  --num-children=ztp,<lambda>
		      The number of children of a node that correponds to a JSON array or
		      object.  The first argument denotes the kind of source of natural
		      numbers; subsequent arguments are specific to the kind of source:
		        fixed   : a fixed, specified value.
		        uniform : a pseudo-random value from a discrete uniform distribution
		                  over a range with the specified bounds.
		        poisson : a pseudo-random value from a Poisson distribution with the
		                  specified lambda parameter.
		        ztp     : a pseudo-random value from a zero-truncated Poisson
		                  distribution with the specified lambda parameter.
		      The default value is 'poisson,5'.
		  --printable-ascii-only
		      Escape characters where necessary so that JSON text contains only
		      printable characters from the US-ASCII character encoding.
		  --proportions=<null>:<boolean>:<int>:<long>:<double>:<string>:<list>:<map>
		      The proportions in which the different kinds of node will be generated.
		      The default proportions are 1:2:2:1:2:8:5:5.
		  --seed=<value>
		      The seed for the pseudo-random number generator.  Only the least
		      significant 48 bits of the seed are used.  If this option is omitted, a
		      'random' seed will be used.
		  --show-info={none|title|tree|results|all}
		      The kinds of information that will be written to the standard output
		      stream.  Multiple kinds may be specified, separated by ','.  The default
		      value is 'tree,results'.
		  --stack-trace
		      Include a stack trace when reporting an exception.
		  --string-length=fixed,<value>
		  --string-length=uniform,<lower-bound>,<upper-bound>
		  --string-length=poisson,<lambda>
		  --string-length=ztp,<lambda>
		      The length of the value of a node that correponds to a JSON string.  The
		      first argument denotes the kind of source of natural numbers; subsequent
		      arguments are specific to the kind of source:
		        fixed   : a fixed, specified value.
		        uniform : a pseudo-random value from a discrete uniform distribution
		                  over a range with the specified bounds.
		        poisson : a pseudo-random value from a Poisson distribution with the
		                  specified lambda parameter.
		        ztp     : a pseudo-random value from a zero-truncated Poisson
		                  distribution with the specified lambda parameter.
		      The default value is ztp,8.
		  --tree-height=<value>
		      The length of the longest path from the root of the JSON tree to a leaf
		      node.  The default value is 8.
		  --use-file
		      Use a temporary file for intermediate storage when generating and parsing
		      JSON text.
		  --validate-xml
		      Validate the generated tree of JSON-XML elements against the JSON-XML
		      schema.  This option has no effect if the '--xml' option is absent.
		  --xml
		      The tests will involve a tree of JSON-XML elements instead of a tree of
		      subclasses of 'AbstractNode'.
		  --xml-namespace-prefix=<prefix>
		      The prefix that is applied to the names of JSON-XML elements and
		      attributes.  This option is ignored if the '--xml' option is absent.
		  --xml-output=<pathname>
		      Write a textual representation of the generated JSON-XML to a file at the
		      specified location.  This option is ignored if the '--xml' option is
		      absent.""";

	/** Values that may be returned by the application when it terminates. */
	private interface ExitCode
	{
		int	SUCCESS	= 0;
		int	ERROR	= 1;
	}

	/** Error messages. */
	private interface ErrorMsg
	{
		String	INVALID_ARGUMENT =
				"'%s' is not a valid argument.";

		String	MALFORMED_OPTION_ARGUMENT =
				"The argument of the '%s' option is malformed.";

		String	CONFLICTING_OPTION_ARGUMENTS =
				"The '%s' option occurs more than once with conflicting arguments.";

		String	MULTIPLE_SUBCOMMANDS =
				"More than one subcommand was specified: %s.";

		String	INVALID_TREE_HEIGHT =
				"The height of the tree is invalid.";

		String	TREE_HEIGHT_OUT_OF_BOUNDS =
				"The height of the tree must be between %d and %d.";

		String	INVALID_SOURCE_KIND =
				"The kind of source is invalid.";

		String	INVALID_FIXED_VALUE =
				"The fixed value is invalid.";

		String	FIXED_VALUE_OUT_OF_BOUNDS =
				"The fixed value must be between %d and %d.";

		String	INVALID_LOWER_BOUND =
				"The lower bound is invalid.";

		String	LOWER_BOUND_OUT_OF_BOUNDS =
				"The lower bound must be between %d and %d.";

		String	INVALID_UPPER_BOUND =
				"The upper bound is invalid.";

		String	UPPER_BOUND_OUT_OF_BOUNDS =
				"The upper bound must be between %d and %d.";

		String	BOUNDS_OUT_OF_ORDER =
				"The lower and upper bounds are out of order.";

		String	INVALID_LAMBDA =
				"The lambda parameter is invalid.";

		String	LAMBDA_OUT_OF_BOUNDS =
				"The lambda parameter must be between %d and %d.";

		String	INVALID_PROPORTIONS =
				"The proportions are invalid.";

		String	SIMPLE_VALUE_PROPORTIONS_ZERO_SUM =
				"The proportion of at least one simple JSON value must be greater than zero.";

		String	COMPOUND_VALUE_PROPORTIONS_ZERO_SUM =
				"The proportion of at least one compound JSON value must be greater than zero.";

		String	INVALID_SEED =
				"The seed is invalid.";

		String	INVALID_INFO_KIND =
				"'%s' is not a valid kind of information.";

		String	INCONSISTENT_INFO_KINDS =
				"The arguments of the '" + Option.SHOW_INFO + "' option are inconsistent.";

		String	ILLEGAL_XML_NAMESPACE_PREFIX =
				"'%s' is not a legal XML namespace prefix.";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The build properties of the application. */
	private	ResourceProperties	buildProperties;

	/** A string representation of the version of this application. */
	private	String				versionStr;

	/** Flag: if {@code true}, the title of the application has been written to the standard output stream. */
	private	boolean				titleShown;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Initialise map of proportions of types of generated node
		DEFAULT_NODE_TYPE_PROPORTIONS = new HashMap<>();
		DEFAULT_NODE_TYPE_PROPORTIONS.put(NullNode.TYPE,    0.02);
		DEFAULT_NODE_TYPE_PROPORTIONS.put(BooleanNode.TYPE, 0.08);
		DEFAULT_NODE_TYPE_PROPORTIONS.put(IntNode.TYPE,     0.08);
		DEFAULT_NODE_TYPE_PROPORTIONS.put(LongNode.TYPE,    0.04);
		DEFAULT_NODE_TYPE_PROPORTIONS.put(DoubleNode.TYPE,  0.06);
		DEFAULT_NODE_TYPE_PROPORTIONS.put(StringNode.TYPE,  0.32);
		DEFAULT_NODE_TYPE_PROPORTIONS.put(ListNode.TYPE,    0.20);
		DEFAULT_NODE_TYPE_PROPORTIONS.put(MapNode.TYPE,     0.20);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of the application.
	 */

	private JsonXmlTestApp()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * This is the main method of the application.
	 *
	 * @param args
	 *          the command-line arguments of the application.
	 */

	public static void main(
		String[]	args)
	{
		System.exit(INSTANCE.run(args));
	}

	//------------------------------------------------------------------

	/**
	 * Parses the specified command-line element to give a source of natural numbers including zero,
	 * &#x2115;<sub>0</sub> (N0), and returns the result.
	 *
	 * @param  element
	 *           the command-line element that will be parsed.
	 * @param  minValue
	 *           the minimum value of a {@link N0Source.Kind#FIXED FIXED} source or a bound of a {@link
	 *           N0Source.Kind#UNIFORM UNIFORM} source.
	 * @param  maxValue
	 *           the maximum value of a {@link N0Source.Kind#FIXED FIXED} source or a bound of a {@link
	 *           N0Source.Kind#UNIFORM UNIFORM} source.
	 * @param  minLambda
	 *           the minimum value of the lambda parameter of a {@link N0Source.Kind#POISSON POISSON} source or a {@link
	 *           N0Source.Kind#ZT_POISSON ZT_POISSON} source.
	 * @param  maxLambda
	 *           the maximum value of the lambda parameter of a {@link N0Source.Kind#POISSON POISSON} source or a {@link
	 *           N0Source.Kind#ZT_POISSON ZT_POISSON} source.
	 * @return the number source that resulted from parsing {@code element}.
	 * @throws BaseException
	 *           if an error occurs when parsing the command-line element.
	 */

	private static N0Source parseN0Source(
		CommandLine.Element<Option>	element,
		int							minValue,
		int							maxValue,
		int							minLambda,
		int							maxLambda)
		throws BaseException
	{
		// Split value of command-line element into fields
		List<String> fields = StringUtils.split(element.getValue(), N0SOURCE_SEPARATOR);
		if (fields.isEmpty())
			throw new OptionException(ErrorMsg.MALFORMED_OPTION_ARGUMENT, element);

		// Decode kind of source from first field
		N0Source.Kind sourceKind = N0Source.Kind.forKey(fields.get(0));
		if (sourceKind == null)
			throw new ArgumentException(ErrorMsg.INVALID_SOURCE_KIND, element);

		// Check for sufficient fields
		if (fields.size() < sourceKind.numParams() + 1)
			throw new OptionException(ErrorMsg.MALFORMED_OPTION_ARGUMENT, element);

		// Decode parameters of source from remaining fields; create source
		N0Source source = null;
		switch (sourceKind)
		{
			case FIXED:
				try
				{
					int value = Integer.parseInt(fields.get(1));
					if ((value < minValue) || (value > maxValue))
						throw new ArgumentException(ErrorMsg.FIXED_VALUE_OUT_OF_BOUNDS, element, minValue, maxValue);
					source = new N0Source.Fixed(value);
				}
				catch (NumberFormatException e)
				{
					throw new ArgumentException(ErrorMsg.INVALID_FIXED_VALUE, element);
				}
				break;

			case UNIFORM:
			{
				// Parse lower bound
				int lowerBound = 0;
				try
				{
					lowerBound = Integer.parseInt(fields.get(1));
					if ((lowerBound < minValue) || (lowerBound > maxValue))
						throw new ArgumentException(ErrorMsg.LOWER_BOUND_OUT_OF_BOUNDS, element, minValue, maxValue);
				}
				catch (NumberFormatException e)
				{
					throw new ArgumentException(ErrorMsg.INVALID_LOWER_BOUND, element);
				}

				// Parse upper bound
				int upperBound = 0;
				try
				{
					upperBound = Integer.parseInt(fields.get(2));
					if ((upperBound < minValue) || (upperBound > maxValue))
						throw new ArgumentException(ErrorMsg.UPPER_BOUND_OUT_OF_BOUNDS, element, minValue, maxValue);
				}
				catch (NumberFormatException e)
				{
					throw new ArgumentException(ErrorMsg.INVALID_UPPER_BOUND, element);
				}

				// Test order of bounds
				if (lowerBound > upperBound)
					throw new ArgumentException(ErrorMsg.BOUNDS_OUT_OF_ORDER, element);

				// Create uniform source
				source = new N0Source.Uniform(lowerBound, upperBound);
				break;
			}

			case POISSON:
				try
				{
					int lambda = Integer.parseInt(fields.get(1));
					if ((lambda < minLambda) || (lambda > maxLambda))
						throw new ArgumentException(ErrorMsg.LAMBDA_OUT_OF_BOUNDS, element, minLambda, maxLambda);
					source = new N0Source.Poisson(lambda);
				}
				catch (NumberFormatException e)
				{
					throw new ArgumentException(ErrorMsg.INVALID_LAMBDA, element);
				}
				break;

			case ZT_POISSON:
				try
				{
					int lambda = Integer.parseInt(fields.get(1));
					if ((lambda < minLambda) || (lambda > maxLambda))
						throw new ArgumentException(ErrorMsg.LAMBDA_OUT_OF_BOUNDS, element, minLambda, maxLambda);
					source = new N0Source.ZtPoisson(lambda);
				}
				catch (NumberFormatException e)
				{
					throw new ArgumentException(ErrorMsg.INVALID_LAMBDA, element);
				}
				break;
		}

		// Return source
		return source;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Runs this application with the specified command-line arguments.
	 *
	 * @param  args
	 *           the command-line arguments.
	 * @return a status code: 0 for success.
	 */

	private int run(
		String[]	args)
	{
		// Assume success
		int exitCode = ExitCode.SUCCESS;

		// Read build properties and initialise version string
		try
		{
			buildProperties =
					new ResourceProperties(ResourceUtils.normalisedPathname(getClass(), BUILD_PROPERTIES_FILENAME));
			versionStr = BuildUtils.versionString(getClass(), buildProperties);
		}
		catch (LocationException e)
		{
			e.printStackTrace();
		}

		// Parse command-line arguments
		boolean stackTrace = false;
		try
		{
			// Initialise local variables
			Set<Option> subcommands = EnumSet.noneOf(Option.class);
			Set<InfoKind> infoKinds = EnumSet.noneOf(InfoKind.class);
			int treeHeight = -1;
			N0Source numChildrenSource = null;
			N0Source stringLengthSource = null;
			boolean allowContainerLeaf = false;
			boolean printableAsciiOnly = false;
			boolean useFile = false;
			boolean xml = false;
			String jsonPathname = null;
			String xmlPathname = null;
			String xmlNamespacePrefix = null;
			boolean validateXml = false;
			Long seed = null;
			Map<NodeType, Double> nodeTypeProportions = null;

			// Parse command-line arguments
			for (CommandLine.Element<Option> element : new CommandLine<>(Option.class, true, USAGE_MESSAGE).parse(args))
			{
				// Get value of command-line element
				String elementValue = element.getValue();

				// Test for option
				if (element.getOption() == null)
					throw new BaseException(ErrorMsg.INVALID_ARGUMENT, elementValue);

				// Parse argument of option
				Option option = element.getOption().getKey();
				switch (option)
				{
					case HELP, RUN_TESTS, VERSION:
						subcommands.add(option);
						break;

					case ALLOW_CONTAINER_LEAF:
						allowContainerLeaf = true;
						break;

					case JSON_OUTPUT:
					{
						String pathname = PathnameUtils.parsePathname(elementValue);
						if ((jsonPathname != null) && !jsonPathname.equals(pathname))
							throw new OptionException(ErrorMsg.CONFLICTING_OPTION_ARGUMENTS, element);
						jsonPathname = pathname;
						break;
					}

					case NUM_CHILDREN:
					{
						N0Source source = parseN0Source(element, MIN_NUM_CHILDREN, MAX_NUM_CHILDREN,
														MIN_NUM_CHILDREN_LAMBDA, MAX_NUM_CHILDREN_LAMBDA);
						if ((numChildrenSource != null) && !numChildrenSource.equals(source))
							throw new OptionException(ErrorMsg.CONFLICTING_OPTION_ARGUMENTS, element);
						numChildrenSource = source;
						break;
					}

					case PRINTABLE_ASCII_ONLY:
						printableAsciiOnly = true;
						break;

					case PROPORTIONS:
						try
						{
							List<String> strs = StringUtils.split(elementValue, ':');
							if (strs.size() != JsonConstants.NODE_TYPES.size())
								throw new ArgumentException(ErrorMsg.INVALID_PROPORTIONS, element);
							Map<NodeType, Double> proportions = new HashMap<>();
							for (int i = 0; i < strs.size(); i++)
							{
								double value = Double.parseDouble(strs.get(i));
								if (value < 0.0)
									throw new ArgumentException(ErrorMsg.INVALID_PROPORTIONS, element);
								proportions.put(JsonConstants.NODE_TYPES.get(i), value);
							}
							if (JsonConstants.SIMPLE_NODE_TYPES.stream()
									.mapToDouble(type -> proportions.get(type))
									.sum() == 0.0)
								throw new ArgumentException(ErrorMsg.SIMPLE_VALUE_PROPORTIONS_ZERO_SUM, element);
							if (JsonConstants.COMPOUND_NODE_TYPES.stream()
									.mapToDouble(type -> proportions.get(type))
									.sum() == 0.0)
								throw new ArgumentException(ErrorMsg.COMPOUND_VALUE_PROPORTIONS_ZERO_SUM, element);

							if ((nodeTypeProportions != null) && !nodeTypeProportions.equals(proportions))
								throw new OptionException(ErrorMsg.CONFLICTING_OPTION_ARGUMENTS, element);
							nodeTypeProportions = proportions;
						}
						catch (NumberFormatException e)
						{
							throw new ArgumentException(ErrorMsg.INVALID_PROPORTIONS, element);
						}
						break;

					case SEED:
						try
						{
							Long value = Long.valueOf(elementValue);
							if ((seed != null) && !seed.equals(value))
								throw new OptionException(ErrorMsg.CONFLICTING_OPTION_ARGUMENTS, element);
							seed = value;
						}
						catch (NumberFormatException e)
						{
							throw new ArgumentException(ErrorMsg.INVALID_SEED, element);
						}
						break;

					case SHOW_INFO:
						for (String key : StringUtils.split(elementValue, InfoKind.SEPARATOR_CHAR))
						{
							if (InfoKind.ALL_KEY.equals(key))
							{
								Stream.of(InfoKind.values())
										.filter(value -> value != InfoKind.NONE)
										.forEach(infoKinds::add);
							}
							else
							{
								InfoKind infoKind = InfoKind.forKey(key);
								if (infoKind == null)
									throw new ArgumentException(ErrorMsg.INVALID_INFO_KIND, element, key);
								infoKinds.add(infoKind);
							}
						}
						if (infoKinds.contains(InfoKind.NONE) && (infoKinds.size() > 1))
							throw new ArgumentException(ErrorMsg.INCONSISTENT_INFO_KINDS, element);
						break;

					case STACK_TRACE:
						stackTrace = true;
						break;

					case STRING_LENGTH:
					{
						N0Source source = parseN0Source(element, MIN_STRING_LENGTH, MAX_STRING_LENGTH,
														MIN_STRING_LENGTH_LAMBDA, MAX_STRING_LENGTH_LAMBDA);
						if ((stringLengthSource != null) && !stringLengthSource.equals(source))
							throw new OptionException(ErrorMsg.CONFLICTING_OPTION_ARGUMENTS, element);
						stringLengthSource = source;
						break;
					}

					case TREE_HEIGHT:
						try
						{
							int value = Integer.parseInt(elementValue);
							if ((treeHeight >= 0) && (treeHeight != value))
								throw new OptionException(ErrorMsg.CONFLICTING_OPTION_ARGUMENTS, element);
							if ((value < MIN_TREE_HEIGHT) || (value > MAX_TREE_HEIGHT))
							{
								throw new ArgumentException(ErrorMsg.TREE_HEIGHT_OUT_OF_BOUNDS, element,
															MIN_TREE_HEIGHT, MAX_TREE_HEIGHT);
							}
							treeHeight = value;
						}
						catch (NumberFormatException e)
						{
							throw new ArgumentException(ErrorMsg.INVALID_TREE_HEIGHT, element);
						}
						break;

					case USE_FILE:
						useFile = true;
						break;

					case VALIDATE_XML:
						validateXml = true;
						break;

					case XML:
						xml = true;
						break;

					case XML_NAMESPACE_PREFIX:
					{
						String prefix = PathnameUtils.parsePathname(elementValue);
						if (prefix.contains(":"))
							throw new ArgumentException(ErrorMsg.ILLEGAL_XML_NAMESPACE_PREFIX, element, prefix);
						if ((xmlNamespacePrefix != null) && !xmlNamespacePrefix.equals(prefix))
							throw new OptionException(ErrorMsg.CONFLICTING_OPTION_ARGUMENTS, element);
						xmlNamespacePrefix = prefix;
						break;
					}

					case XML_OUTPUT:
					{
						String pathname = PathnameUtils.parsePathname(elementValue);
						if ((xmlPathname != null) && !xmlPathname.equals(pathname))
							throw new OptionException(ErrorMsg.CONFLICTING_OPTION_ARGUMENTS, element);
						xmlPathname = pathname;
						break;
					}
				}
			}

			// Ensure a single subcommand
			if (subcommands.size() > 1)
			{
				String names = subcommands.stream().map(Option::getName).collect(Collectors.joining(", "));
				throw new BaseException(ErrorMsg.MULTIPLE_SUBCOMMANDS, names);
			}
			if (subcommands.isEmpty())
				subcommands.add(Option.RUN_TESTS);

			// Perform subcommand
			Option subcommand = subcommands.iterator().next();
			switch (subcommand)
			{
				case HELP:
					showTitle();
					System.out.println();
					System.out.println(USAGE_MESSAGE);
					break;

				case RUN_TESTS:
				{
					// Set default values for missing options
					if (seed == null)
						seed = new Random().nextLong();
					if (infoKinds.isEmpty())
						infoKinds.addAll(InfoKind.DEFAULT_VALUES);
					if (treeHeight < 0)
						treeHeight = DEFAULT_TREE_HEIGHT;
					if (numChildrenSource == null)
						numChildrenSource = DEFAULT_NUM_CHILDREN_SOURCE;
					if (stringLengthSource == null)
						stringLengthSource = DEFAULT_STRING_LENGTH_SOURCE;
					if (nodeTypeProportions == null)
						nodeTypeProportions = DEFAULT_NODE_TYPE_PROPORTIONS;

					// Write title to standard output stream
					if (infoKinds.contains(InfoKind.TITLE))
						showTitle();

					// Get parameters of tests
					Path jsonFile = (jsonPathname == null) ? null : Path.of(jsonPathname);
					boolean writeInputTree = infoKinds.contains(InfoKind.TREE);
					boolean writeResults = infoKinds.contains(InfoKind.RESULTS);

					// Case: test parsing and generation of JSON text via JSON-XML
					if (xml)
					{
						Path xmlFile = (xmlPathname == null) ? null : Path.of(xmlPathname);
						JsonXmlTests tester = new JsonXmlTests();
						tester.createInputTree(seed, xmlNamespacePrefix, treeHeight, numChildrenSource,
											   stringLengthSource, nodeTypeProportions, allowContainerLeaf,
											   writeInputTree);
						tester.testRoundTrip(printableAsciiOnly, useFile, jsonFile, xmlFile, validateXml, writeResults);
						tester.testCopyTree(writeResults);
						if (xmlNamespacePrefix != null)
							tester.testCopyTreeIgnoreNS(writeResults);
					}

					// Case: test parsing and generation of JSON text via tree of AbstractNode
					else
					{
						JsonTests tester = new JsonTests();
						tester.createInputTree(seed, treeHeight, numChildrenSource, stringLengthSource,
											   nodeTypeProportions, allowContainerLeaf, writeInputTree);
						tester.testRoundTrip(printableAsciiOnly, useFile, jsonFile, writeResults);
						tester.testCopyTree(writeResults);
					}
					break;
				}

				case VERSION:
					showTitle();
					break;

				default:
					throw new UnexpectedRuntimeException(subcommand.name);
			}
		}
		catch (Throwable e)
		{
			// Write title
			if (!titleShown)
				System.err.println(SHORT_NAME + " " + versionStr);

			// Case: write stack trace
			if (stackTrace)
				e.printStackTrace();

			// Case: write exception and causes of exception
			else
			{
				// Write exception
				System.err.println(((e instanceof BaseException) || (e instanceof CommandLine.CommandLineException))
										? e.getMessage()
										: e);

				// Write causes of exception
				String causeStr = ExceptionUtils.getCompositeCauseString(e.getCause(), EXCEPTION_CAUSE_PREFIX);
				if (!causeStr.isEmpty())
					System.err.println(causeStr);
			}

			// Set exit code
			exitCode = ExitCode.ERROR;
		}

		// Return exit code
		return exitCode;
	}

	//------------------------------------------------------------------

	/**
	 * Writes the title of this application to the standard output stream if it hasn't already been written.
	 */

	private void showTitle()
	{
		if (!titleShown)
		{
			System.out.println(SHORT_NAME + " " + versionStr);
			titleShown = true;
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: COMMAND-LINE OPTIONS


	/**
	 * This is an enumeration of the command-line options of the application.  The name of an option must have the
	 * prefix "--" when it appears on the command line, unless the option is a subcommand, when the "--" prefix may be
	 * omitted.  There is no short form for any of the options.
	 */

	private enum Option
		implements CommandLine.IOption<Option>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/**
		 * Flag: allow a JSON array or object to be a leaf node.
		 */
		ALLOW_CONTAINER_LEAF
		(
			"allow-container-leaf",
			false,
			false
		),

		/**
		 * Subcommand: write information about the application to the standard output stream.
		 */
		HELP
		(
			"help",
			true,
			false
		),

		/**
		 * Write the generated JSON text to a file at the specified location.
		 */
		JSON_OUTPUT
		(
			"json-output",
			false,
			true
		),

		/**
		 * The number of children of a generated JSON array or object.
		 */
		NUM_CHILDREN
		(
			"num-children",
			false,
			true
		),

		/**
		 * Flag: escape characters where necessary so that JSON text contains only printable characters from the
		 * US-ASCII character encoding.
		 */
		PRINTABLE_ASCII_ONLY
		(
			"printable-ascii-only",
			false,
			false
		),

		/**
		 * The proportions in which the different types of node will be generated.
		 */
		PROPORTIONS
		(
			"proportions",
			false,
			true
		),

		/**
		 * Subcommand: run the tests.
		 */
		RUN_TESTS
		(
			"run-tests",
			true,
			false
		),

		/**
		 * The seed for the pseudo-random number generator.
		 */
		SEED
		(
			"seed",
			false,
			true
		),

		/**
		 * The kinds of information that will be written to the standard output stream.
		 */
		SHOW_INFO
		(
			"show-info",
			false,
			true
		),

		/**
		 * Flag: include a stack trace when reporting an exception.
		 */
		STACK_TRACE
		(
			"stack-trace",
			false,
			false
		),

		/**
		 * The length of a generated JSON string.
		 */
		STRING_LENGTH
		(
			"string-length",
			false,
			true
		),

		/**
		 * The length of the longest path from the root of the JSON tree to a leaf node.
		 */
		TREE_HEIGHT
		(
			"tree-height",
			false,
			true
		),

		/**
		 * Flag: use a file as an intermediate when generating and parsing JSON text.
		 */
		USE_FILE
		(
			"use-file",
			false,
			false
		),

		/**
		 * Flag: validate the generated XML tree against the JSON-XML schema.
		 */
		VALIDATE_XML
		(
			"validate-xml",
			false,
			false
		),

		/**
		 * Subcommand: write the name and version of the application to the standard output stream.
		 */
		VERSION
		(
			"version",
			true,
			false
		),

		/**
		 * Flag: test the generation and parsing of JSON-XML.
		 */
		XML
		(
			"xml",
			false,
			false
		),

		/**
		 * The prefix that is applied to the names of JSON-XML elements and attributes.
		 */
		XML_NAMESPACE_PREFIX
		(
			"xml-namespace-prefix",
			false,
			true
		),

		/**
		 * Write a textual representation of the generated JSON-XML to a file at the specified location.
		 */
		XML_OUTPUT
		(
			"xml-output",
			false,
			true
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The name of this option. */
		private	String	name;

		/** Flag: if {@code true}, this option is a subcommand. */
		private	boolean	subcommand;

		/** Flag: if {@code true}, this option requires an argument. */
		private	boolean	requiresArgument;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for a command-line option of the application.
		 *
		 * @param name
		 *          the name of the option.
		 * @param subcommand
		 *          if {@code true}, the option is a subcommand.
		 * @param requiresArgument
		 *          if {@code true}, the option requires an argument.
		 */

		private Option(
			String	name,
			boolean	subcommand,
			boolean	requiresArgument)
		{
			// Initialise instance variables
			this.name = name;
			this.subcommand = subcommand;
			this.requiresArgument = requiresArgument;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : CommandLine.IOption interface
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public Option getKey()
		{
			return this;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public String getName()
		{
			return name;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public boolean isSubcommand()
		{
			return subcommand;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public boolean requiresArgument()
		{
			return requiresArgument;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return getPrefixedName();
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ENUMERATION: KINDS OF INFORMATION


	/**
	 * This is an enumeration of kinds of information that may be written by the application to the standard output
	 * stream.
	 */

	private enum InfoKind
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////


		/**
		 * No information.
		 */
		NONE
		(
			"none"
		),

		/**
		 * The input tree.
		 */
		TREE
		(
			"tree"
		),

		/**
		 * The title of the application.
		 */
		TITLE
		(
			"title"
		),

		/**
		 * The results of the application.
		 */
		RESULTS
		(
			"results"
		);

		/** The separator between kinds of information in the argument of the 'show-info' command-line option. */
		private static final	char	SEPARATOR_CHAR	= ',';

		/** The key that denotes all kinds of information. */
		private static final	String	ALL_KEY	= "all";

		/** The default kinds of information. */
		private static final	Set<InfoKind>	DEFAULT_VALUES	= EnumSet.of(TREE, RESULTS);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The key that is associated with this kind of information. */
		private	String	key;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for a kind of information that may be written to the
		 * standard output stream.
		 *
		 * @param key
		 *          the key that will be associated with this kind of information.
		 */

		private InfoKind(
			String	key)
		{
			// Initialise instance variables
			this.key = key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the kind of information that is associated with the specified key.
		 *
		 * @param  key
		 *           the key whose associated kind of information is desired.
		 * @return the kind of information that is associated {@code key}, or {@code null} if there is no such kind of
		 *         information.
		 */

		private static InfoKind forKey(
			String	key)
		{
			return Stream.of(values())
					.filter(value -> value.key.equalsIgnoreCase(key))
					.findFirst()
					.orElse(null);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: COMMAND-LINE OPTION EXCEPTION


	/**
	 * This class implements an exception that relates to a command-line option.
	 */

	private static class OptionException
		extends BaseException
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an exception that has the specified detail message and relates to the option that
		 * is associated with the specified command-line element.
		 *
		 * @param message
		 *          the detail message of the exception.
		 * @param element
		 *          the command-line element to whose associated option the exception relates.
		 */

		private OptionException(
			String						message,
			CommandLine.Element<Option>	element)
		{
			// Call superclass constructor
			super(message, element.getOptionString());
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: COMMAND-LINE ARGUMENT EXCEPTION


	/**
	 * This class implements an exception that relates to a command-line argument.
	 */

	private static class ArgumentException
		extends BaseException
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an exception that has the specified detail message and relates to the specified
		 * command-line element.
		 *
		 * @param message
		 *          the detail message of the exception.
		 * @param element
		 *          the command-line element to which the exception relates.
		 * @param replacements
		 *          the items whose string representations will replace placeholders in {@code message}.
		 */

		private ArgumentException(
			String						message,
			CommandLine.Element<Option>	element,
			Object...					replacements)
		{
			// Call superclass constructor
			super(element.getOptionString() + "=" + element.getValue() + "\n" + createMessage(message, replacements));
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
