/*====================================================================*\

TestsBase.java

Class: abstract base class of JSON-related tests.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.jsonxmltest;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.nio.file.Files;
import java.nio.file.Path;

import java.text.DecimalFormat;

import java.util.List;
import java.util.Map;

import java.util.random.RandomGenerator;

import uk.blankaspect.common.basictree.NodeType;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;

import uk.blankaspect.common.jsonxml.ElementKind;

//----------------------------------------------------------------------


// CLASS: ABSTRACT BASE CLASS OF JSON-RELATED TESTS


/**
 * This abstract class contains constants, variables, methods and types that are common to JSON-related tests.
 */

abstract class TestsBase
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The mask of the seed of the pseudo-random number generator. */
	protected static final	long	SEED_MASK	= (1L << 48) - 1;

	/** The maximum length of a line of generated JSON text. */
	protected static final	int		JSON_MAX_LINE_LENGTH	= 128;

	/** The name of the temporary directory. */
	protected static final	String	TEMP_DIRECTORY_NAME	= MethodHandles.lookup().lookupClass().getPackageName();

	/** The filename extension of a JSON file. */
	protected static final	String	JSON_FILENAME_EXTENSION	= ".json";

	/** The formatter that is applied to integer values to group digits in threes. */
	protected static final	DecimalFormat	INTEGER_FORMATTER;

	/** The formatter of the relative frequency of a type of JSON value as it appears in the results that are written to
		the standard output stream. */
	protected static final	DecimalFormat	FREQ_FORMATTER	= new DecimalFormat("0.000");

	/** The lower bound of the first range of characters that may appear in a generated JSON string. */
	private static final	int		CHAR_LOWER_BOUND1	= 0x20;

	/** The upper bound of the first range of characters that may appear in a generated JSON string. */
	private static final	int		CHAR_UPPER_BOUND1	= 0x7E;

	/** The lower bound of the second range of characters that may appear in a generated JSON string. */
	private static final	int		CHAR_LOWER_BOUND2	= 0xA0;

	/** The upper bound of the second range of characters that may appear in a generated JSON string. */
	private static final	int		CHAR_UPPER_BOUND2	= 0xFF;

	/** The fraction of the characters of a generated JSON string that will, in the long run, be selected from the
		second (non-ASCII) range. */
	private static final	double	NON_ASCII_CHAR_FRACTION	= 0.04;

	/** Miscellaneous strings. */
	protected static final	String	LINE_SEPARATOR_STR	= "-".repeat(32);
	protected static final	String	EQUALS_STR			= " = ";
	protected static final	String	COLON_STR			= " : ";
	protected static final	String	TOTAL_STR			= "TOTAL";
	protected static final	String	INPUT_TREE_STR		= "Input tree";
	protected static final	String	OUTPUT_TREE_STR		= "Output tree";
	protected static final	String	COPY_TREE_STR		= "Copy tree";
	protected static final	String	ROUND_TRIP_STR		= "Round trip";
	protected static final	String	MATCH_STR			= "MATCH";
	protected static final	String	NO_MATCH_STR		= "NO MATCH";
	protected static final	String	SEED_STR			= "seed";
	protected static final	String	HEIGHT_STR			= "height";
	protected static final	String	NO_INPUT_TREE_STR	= "No input tree";

	/** Keys of system properties. */
	private interface SystemPropertyKey
	{
		String	TEMP_DIR	= "java.io.tmpdir";
	}

	/** Error messages. */
	protected interface ErrorMsg
	{
		String	FAILED_TO_GET_LOCATION_OF_TEMPORARY_DIRECTORY =
				"Failed to get the location of the system's temporary directory.";

		String	FAILED_TO_CREATE_TEMPORARY_DIRECTORY =
				"Failed create a temporary directory.";

		String	FAILED_TO_CREATE_TEMPORARY_FILE =
				"Failed create a temporary file.";

		String	ERROR_READING_FILE =
				"An error occurred when reading the file.";

		String	ERROR_WRITING_FILE =
				"An error occurred when writing the file.";

		String	ERROR_PARSING_JSON_TEXT =
				"An error occurred when parsing the JSON text.";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The pseudo-random number generator that is used in the generation of the tree of JSON values. */
	protected	RandomGenerator	prng;

	/** An array of spaces that is used to indent results that are written to the standard output stream. */
	protected	char[]			spaces;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		INTEGER_FORMATTER = new DecimalFormat();
		INTEGER_FORMATTER.setGroupingSize(3);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a container of constants, variables, methods and types that are common to JSON-related
	 * tests.
	 */

	protected TestsBase()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a subdirectory of the system temporary directory with the specified name and returns its location.  This
	 * method has no effect if the named subdirectory already exists within the system temporary directory.
	 *
	 * @param  name
	 *           the name of the subdirectory.
	 * @return the location of a subdirectory of the system temporary directory whose name is {@code name}.
	 * @throws BaseException
	 *           if an error occurs when creating the directory.
	 */

	protected static Path createTempDirectory(
		String	name)
		throws BaseException
	{
		// Get pathname of default temporary directory
		String tempDirPathname = System.getProperty(SystemPropertyKey.TEMP_DIR);
		if (tempDirPathname == null)
			throw new BaseException(ErrorMsg.FAILED_TO_GET_LOCATION_OF_TEMPORARY_DIRECTORY);

		// Create temporary directory
		Path directory = Path.of(tempDirPathname, name);
		try
		{
			Files.createDirectories(directory);
		}
		catch (Exception e)
		{
			throw new FileException(ErrorMsg.FAILED_TO_CREATE_TEMPORARY_DIRECTORY, e, directory);
		}

		// Return location of temporary directory
		return directory;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the cumulative distribution function (CDF) for the specified collection of {@linkplain NodeType types of
	 * node}.
	 *
	 * @param  nodeTypes
	 *           the types of node whose cumulative distribution function is desired.
	 * @param  nodeTypeProportions
	 *           a map from types of node to their expected proportions in a generated tree.
	 * @param  nodeTypeCdfs
	 *           a map from collections of types of node to cumulative distribution functions that have been created
	 *           from {@code nodeTypeProportions}.  If the map does not contain an entry for {@code nodeTypes}, one will
	 *           be created and added to the map.
	 * @return the cumulative distribution function for {@code nodeTypes}.
	 */

	protected static NodeTypeProbability[] nodeTypeCdf(
		List<NodeType>								nodeTypes,
		Map<NodeType, Double>						nodeTypeProportions,
		Map<List<NodeType>, NodeTypeProbability[]>	nodeTypeCdfs)
	{
		NodeTypeProbability[] cdf = nodeTypeCdfs.get(nodeTypes);
		if (cdf == null)
		{
			cdf = new NodeTypeProbability[nodeTypes.size()];
			double sum = 0.0;
			int index = 0;
			for (NodeType nodeType : nodeTypes)
			{
				sum += nodeTypeProportions.get(nodeType);
				cdf[index++] = new NodeTypeProbability(nodeType, sum);
			}

			double factor = 1.0 / sum;
			for (int i = 0; i < cdf.length; i++)
				cdf[i].probability *= factor;
			cdf[cdf.length - 1].probability = 1.0;

			nodeTypeCdfs.put(nodeTypes, cdf);
		}
		return cdf;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Generates a pseudo-random double-precision floating-point number by using {@link #prng}, and returns the result.
	 *
	 * @return a pseudo-random double-precision floating-point number.
	 */

	protected double generateDouble()
	{
		return prng.nextBoolean()
						? Double.longBitsToDouble(prng.nextLong() & 0xFFEFFFFFFFFFFFFFL)
						: (double)prng.nextInt(1000) * prng.nextDouble();
	}

	//------------------------------------------------------------------

	/**
	 * Generates a string of the specified number of pseudo-random characters by using {@link #prng}, and returns the
	 * result.
	 *
	 * @param  length
	 *           the length of the generated string.
	 * @return a string of {@code length} pseudo-random characters.
	 */

	protected String generateString(
		int	length)
	{
		StringBuilder buffer = new StringBuilder(length);
		for (int i = 0; i < length; i++)
		{
			char ch = (prng.nextDouble() < NON_ASCII_CHAR_FRACTION)
								? (char)(CHAR_LOWER_BOUND2 + prng.nextInt(CHAR_UPPER_BOUND2 - CHAR_LOWER_BOUND2 + 1))
								: (char)(CHAR_LOWER_BOUND1 + prng.nextInt(CHAR_UPPER_BOUND1 - CHAR_LOWER_BOUND1 + 1));
			buffer.append(ch);
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: PAIRING OF NODE TYPE AND CUMULATIVE PROBABILITY


	/**
	 * This class implements a pairing of a {@linkplain NodeType type of node} and an associated cumulative probability.
	 */

	static class NodeTypeProbability
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The type of node. */
		protected NodeType	nodeType;

		/** The cumulative probability. */
		protected double	probability;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a pairing of a {@linkplain NodeType type of node} and an associated cumulative
		 * probability.
		 *
		 * @param nodeType
		 *          the type of node.
		 * @param probability
		 *          the cumulative probability.
		 */

		protected NodeTypeProbability(
			NodeType	nodeType,
			double		probability)
		{
			// Initialise instance variables
			this.nodeType = nodeType;
			this.probability = probability;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: INFORMATION ABOUT A KIND OF NODE


	/**
	 * This class implements information about the occurrences of a kind of node &mdash; either a {@linkplain NodeType
	 * type of <code>AbstractNode</code>} or a {@linkplain ElementKind kind of JSON-XML element} &mdash; that are
	 * encountered when traversing a tree.  The kind of node corresponds to a kind of JSON value: null, Boolean, number,
	 * string, array or object.
	 */

	static class NodeKindInfo
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The count of nodes of the relevant kind. */
		protected	long	count;

		/** The maximum depth of a node of the relevant kind. */
		protected	int		maxDepth;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of information about the occurrences of a kind of node that are encountered when
		 * traversing a tree.
		 */

		protected NodeKindInfo()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Updates the maximum depth of a node from the specified value.
		 *
		 * @param depth
		 *          the depth of a node.
		 */

		protected void updateMaxDepth(
			int	depth)
		{
			if (maxDepth < depth)
				maxDepth = depth;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
