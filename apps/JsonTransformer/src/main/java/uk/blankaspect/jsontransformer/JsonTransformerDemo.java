/*====================================================================*\

JsonTransformerDemo.java

Class: JSON-transformer demonstration application.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.jsontransformer;

//----------------------------------------------------------------------


// IMPORTS


import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import java.util.random.RandomGenerator;

import javax.xml.transform.Transformer;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.BasicTransformerFactory;

import org.w3c.dom.Element;

import uk.blankaspect.common.basictree.AbstractNode;
import uk.blankaspect.common.basictree.ListNode;
import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;
import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.json.JsonGenerator;
import uk.blankaspect.common.json.JsonParser;

import uk.blankaspect.common.jsonxml.IElementFacade;
import uk.blankaspect.common.jsonxml.JsonGeneratorXml;
import uk.blankaspect.common.jsonxml.JsonXmlUtils;
import uk.blankaspect.common.jsonxml.JsonXmlValidator;
import uk.blankaspect.common.jsonxml.SimpleElementFacade;

//----------------------------------------------------------------------


// CLASS: JSON-TRANSFORMER DEMONSTRATION APPLICATION


/**
 * This is the main class of a command-line application that demonstrates the use of classes from the {@code
 * uk.blankaspect.common.json} and {@code uk.blankaspect.common.jsonxml} packages to parse, transform and generate JSON
 * text.
 * <p>
 * The transformation of JSON text is achieved by converting it to a simple XML format (referred to as JSON-XML),
 * applying an XSL transformation to the XML document and converting the result back to JSON text.
 * </p>
 * <p>
 * The application performs the steps listed below.  If the location of an output directory is supplied as a
 * command-line argument, four files are written to the output directory: JSON input, JSON-XML input, JSON output and
 * JSON-XML output.  The file-writing steps are marked as optional.
 * </p>
 * <ol>
 *   <li>
 *     Create a tree of {@linkplain AbstractNode nodes} as the precursor of JSON text.  The tree is mostly predefined,
 *     with some randomness.
 *   </li>
 *   <li>
 *     Generate JSON text from the tree.
 *   </li>
 *   <li>
 *     Write the JSON text to a file (optional).
 *   </li>
 *   <li>
 *     Create the input JSON-XML document and document element.
 *   </li>
 *   <li>
 *     Parse the JSON text to create a tree of JSON-XML elements.
 *   </li>
 *   <li>
 *     Add the tree of JSON-XML elements to the document element from step 4.
 *   </li>
 *   <li>
 *     Write the JSON-XML document to a file (optional).
 *   </li>
 *   <li>
 *     Create the output JSON-XML document and document element.
 *   </li>
 *   <li>
 *     Transform the JSON-XML tree with a predefined XSL transformation.
 *   </li>
 *   <li>
 *     Validate the resulting JSON-XML tree against an XSD schema.
 *   </li>
 *   <li>
 *     Generate the output XML text from the tree of JSON-XML elements.
 *   </li>
 *   <li>
 *     Write the output XML document to a file (optional).
 *   </li>
 *   <li>
 *     Convert the output XML to JSON text.
 *   </li>
 *   <li>
 *     Write the output JSON text to a file (optional).
 *   </li>
 * </ol>
 */

public class JsonTransformerDemo
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The single instance of this class. */
	private static final	JsonTransformerDemo	INSTANCE	= new JsonTransformerDemo();

	/** The name of the document element of a JSON-XML document. */
	private static final	String	DOCUMENT_ELEMENT_NAME	= "json-xml";

	/** The XML declaration. */
	private static final	String	XML_DECLARATION	= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	/** The start tag of a JSON-XML document. */
	private static final	String	XML_DOCUMENT_START_TAG	= "<" + DOCUMENT_ELEMENT_NAME + ">\n";

	/** The end tag of a JSON-XML document. */
	private static final	String	XML_DOCUMENT_END_TAG	= "</" + DOCUMENT_ELEMENT_NAME + ">\n";

	/** The number of spaces by which each additional level of the XML tree will be indented. */
	private static final	int		XML_INDENT_INCREMENT	= 2;

	/** The filename stem of input and output files. */
	private static final	String	FILENAME_STEM	= "nobelLaureates";

	/** The name of the XSL transformation file. */
	private static final	String	XSL_FILENAME	= FILENAME_STEM + ".xsl";

	/** The name of the JSON input file. */
	private static final	String	JSON_INPUT_FILENAME		= FILENAME_STEM + ".json";

	/** The name of the JSON output file. */
	private static final	String	JSON_OUTPUT_FILENAME	= FILENAME_STEM + "-out.json";

	/** The name of the XML input file. */
	private static final	String	XML_INPUT_FILENAME		= FILENAME_STEM + ".xml";

	/** The name of the XML output file. */
	private static final	String	XML_OUTPUT_FILENAME		= FILENAME_STEM + "-out.xml";

	/** The recognised prefixes of a key of a Java system property or the name of an environment variable in a
		substitution. */
	private static final	String[]	PROPERTY_KEY_PREFIXES	=
	{
		"${",
		"\u00A2{"	// cent sign
	};

	/** The suffix of a key of a Java system property or the name of an environment variable in a substitution. */
	private static final	String	PROPERTY_KEY_SUFFIX	= "}";

	/** The prefix of a property key that indicates that the remainder of the key is the name of an environment
		variable. */
	private static final	String	ENV_PREFIX	= "env.";

	/** The prefix of a property key that indicates that the remainder of the key is the key of a Java system
		property. */
	private static final	String	SYS_PREFIX	= "sys.";

	/** A list of awards of Nobel prizes. */
	private static final	List<Award>	AWARDS	= List.of
	(
		new Award(Field.CHEMISTRY,  1960, new Person("Libby",       "Willard")),
		new Award(Field.CHEMISTRY,  1961, new Person("Calvin",      "Melvin")),
		new Award(Field.CHEMISTRY,  1962, new Person("Perutz",      "Max")),
		new Award(Field.CHEMISTRY,  1963, new Person("Ziegler",     "Karl")),
		new Award(Field.CHEMISTRY,  1964, new Person("Hodgkin",     "Dorothy")),
		new Award(Field.PHYSICS,    1960, new Person("Glaser",      "Donald")),
		new Award(Field.PHYSICS,    1961, new Person("Hofstadter",  "Robert")),
		new Award(Field.PHYSICS,    1962, new Person("Landau",      "Lev")),
		new Award(Field.PHYSICS,    1963, new Person("Wigner",      "Eugene")),
		new Award(Field.PHYSICS,    1964, new Person("Basov",       "Nikolay")),
		new Award(Field.LITERATURE, 1960, new Person("Perse",       "Saint-John")),
		new Award(Field.LITERATURE, 1961, new Person("Andri\u0107", "Ivo")),
		new Award(Field.LITERATURE, 1962, new Person("Steinbeck",   "John")),
		new Award(Field.LITERATURE, 1963, new Person("Seferis",     "Giorgos")),
		new Award(Field.LITERATURE, 1964, new Person("Sartre",      "Jean-Paul"))
	);

	/** Miscellaneous strings. */
	private static final	String	WRITING_STR				= "Writing";
	private static final	String	NO_OUTPUT_DIRECTORY_STR	= "The location of an output directory was not supplied.\n"
																+ "No files will be written.";

	/** Keys of properties. */
	private interface PropertyKey
	{
		String	PRIMARY_NAME	= "primaryName";
		String	SECONDARY_NAME	= "secondaryName";
		String	WINNER			= "winner";
		String	YEAR			= "year";
	}

	/** Error messages. */
	private interface ErrorMsg
	{
		String	NOT_A_DIRECTORY				= "The location does not denote a directory.";
		String	FAILED_TO_CREATE_DIRECTORY	= "Failed to create the directory.";
		String	ERROR_PARSING_JSON_TEXT		= "An error occurred when parsing the JSON text.";
		String	ERROR_GENERATING_XML_TEXT	= "An error occurred when generating XML text.";
		String	ERROR_TRANSFORMING_XML_TREE	= "An error occurred when transforming the tree of XML elements.";
		String	ERROR_WRITING_FILE			= "An error occurred when writing the file.";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of the application.
	 */

	private JsonTransformerDemo()
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
	 * Replaces each occurrence of the key of a Java system property or the name of an environment variable in the
	 * specified string with the value of the variable or property and returns the result.  The name of the environment
	 * variable or the key of the system property must be enclosed between '${' and '}'; eg, <code>${HOME}</code>.  The
	 * cent sign (U+00A2) may be used instead of '$'.  A Java system property takes precedence over an environment
	 * variable.
	 * <ul>
	 *   <li>
	 *     A Java system property can be specified explicitly with the prefix 'sys.'; for example,
	 *     <code>${sys.user.home}</code> .
	 *   </li>
	 *   <li>
	 *     An environment variable can be specified explicitly with the prefix 'env.'; for example,
	 *     <code>${env.HOME}</code> .
	 *   </li>
	 * </ul>
	 *
	 * @param  str
	 *           the string on which the substitution of environment variables and Java system properties will be
	 *           performed.
	 * @return {@code str} after the substitution of environment variables and Java system properties.
	 */

	private static String substituteProperties(
		String	str)
	{
		// Kinds of span
		enum SpanKind
		{
			UNKNOWN,
			LITERAL,
			ENVIRONMENT,
			SYSTEM
		}

		// Declare record for span
		record Span(
			SpanKind	kind,
			String		key,
			String		text)
		{ }

		// Initialise list of spans
		List<Span> spans = new ArrayList<>();

		// Decompose input text into spans
		int index = 0;
		while (index < str.length())
		{
			// Initialise local variables
			SpanKind spanKind = SpanKind.UNKNOWN;
			String spanKey = null;
			String spanText = null;
			int keyPrefixLength = 0;
			int startIndex = index;

			// Search for start of property key
			for (String keyPrefix : PROPERTY_KEY_PREFIXES)
			{
				index = str.indexOf(keyPrefix, startIndex);
				if (index >= 0)
				{
					keyPrefixLength = keyPrefix.length();
					break;
				}
			}

			// If no property key was found, add span for literal text ...
			if (index < 0)
			{
				index = str.length();
				if (index > startIndex)
				{
					spanKind = SpanKind.LITERAL;
					spanText = str.substring(startIndex);
				}
			}

			// ... otherwise, search for end of property key; replace property key with value of property
			else
			{
				// Search for end of property key
				index = str.indexOf(PROPERTY_KEY_SUFFIX, index + keyPrefixLength);

				// If there is no property-key suffix, add span for literal text ...
				if (index < 0)
				{
					index = str.length();
					if (index > startIndex)
					{
						spanKind = SpanKind.LITERAL;
						spanText = str.substring(startIndex);
					}
				}

				// ... otherwise, replace property key with value of property
				else
				{
					// Extract property key
					startIndex += keyPrefixLength;
					String key = str.substring(startIndex, index);

					// Replace property key with value of property
					try
					{
						// Case: key is environment variable
						if (key.startsWith(ENV_PREFIX))
						{
							spanKind = SpanKind.ENVIRONMENT;
							spanKey = key.substring(ENV_PREFIX.length());
							spanText = System.getenv(spanKey);
						}

						// Case: key is key of system property
						else if (key.startsWith(SYS_PREFIX))
						{
							spanKind = SpanKind.SYSTEM;
							spanKey = key.substring(SYS_PREFIX.length());
							spanText = System.getProperty(spanKey);
						}

						// Case: key does not start with a recognised prefix
						else
						{
							// Try system property, then environment variable
							spanKey = key;
							if (!key.isEmpty())
							{
								String value = System.getProperty(key);
								if (value == null)
								{
									value = System.getenv(key);
									if (value != null)
									{
										spanKind = SpanKind.ENVIRONMENT;
										spanText = value;
									}
								}
								else
								{
									spanKind = SpanKind.SYSTEM;
									spanText = value;
								}
							}
						}
					}
					catch (SecurityException e)
					{
						// ignore
					}

					// Increment index past end of property key
					index += PROPERTY_KEY_SUFFIX.length();
				}
			}

			// Create new span and add it to list
			spans.add(new Span(spanKind, spanKey, spanText));
		}

		// Concatenate spans
		StringBuilder buffer = new StringBuilder(256);
		for (Span span : spans)
		{
			if (span.text != null)
				buffer.append(span.text);
		}

		// Return result
		return buffer.toString();
	}

	//------------------------------------------------------------------

	/**
	 * Creates a tree of {@linkplain AbstractNode nodes} that is the precursor of JSON text and returns the root of the
	 * tree.  The principal nodes of the tree represent the awards of Nobel Prizes in several fields over a range of
	 * years.  To add some variation, one randomly selected award is removed from each field.
	 *
	 * @return the root of the tree of nodes that is the precursor of JSON text.
	 */

	private static MapNode createInputTree()
	{
		// Create root node of tree
		MapNode rootNode = new MapNode();

		// Find minimum awards per field
		int minAwardsPerField = AWARDS.size();
		for (Field field : Field.values())
		{
			int awardsPerField = (int)AWARDS.stream().filter(award -> award.field == field).count();
			if (awardsPerField < Field.values().length)
				throw new UnexpectedRuntimeException("Too few awards per field: " + field);
			minAwardsPerField = Math.min(awardsPerField, minAwardsPerField);
		}

		// Initialise list of indices of awards for field
		List<Integer> awardIndices = new ArrayList<>();
		for (int i = 0; i < minAwardsPerField; i++)
			awardIndices.add(i);

		// Generate remaining nodes of tree
		RandomGenerator prng = new Random();
		for (Field field : Field.values())
		{
			// Create list of awards for field
			List<Award> awards = new ArrayList<>();
			for (Award award : AWARDS)
			{
				if (award.field == field)
					awards.add(award);
			}

			// Remove a randomly selected award
			awards.remove(awardIndices.remove(prng.nextInt(awardIndices.size())).intValue());

			// Sort awards by year
			awards.sort(Comparator.comparing(Award::year));

			// Encode awards
			ListNode awardsNode = rootNode.addList(field.getKey());
			for (Award award : awards)
			{
				// Create node: award
				MapNode awardNode = new MapNode();
				awardsNode.add(awardNode);

				// Encode year of award
				awardNode.addInt(PropertyKey.YEAR, award.year);

				// Create node: winner
				MapNode winnerNode = new MapNode();
				awardNode.add(PropertyKey.WINNER, winnerNode);

				// Encode primary name of winner
				winnerNode.addString(PropertyKey.PRIMARY_NAME, award.winner.primaryName);

				// Encode secondary name of winner
				winnerNode.addString(PropertyKey.SECONDARY_NAME, award.winner.secondaryName);
			}
		}

		// Return root node of tree
		return rootNode;
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a textual representation of an JSON-XML document that contains the tree of JSON-XML elements
	 * whose root is the specified element.
	 *
	 * @param  rootElement
	 *           the root of the tree of JSON-XML elements for which a textual representation is desired.
	 * @return a textual representation of the JSON-XML document that contains the tree of JSON-XML elements whose root
	 *         is {@code rootElement}.
	 * @throws BaseException
	 *           if an error occurs when generating the textual representation.
	 */

	private static String createXmlDocumentText(
		Element	rootElement)
		throws BaseException
	{
		try
		{
			// Create writer
			CharArrayWriter writer = new CharArrayWriter();

			// Write XML declaration
			writer.write(XML_DECLARATION);

			// Write start tag of document element
			writer.write(XML_DOCUMENT_START_TAG);

			// Write tree of JSON-XML elements
			JsonXmlUtils.writeXml(rootElement, XML_INDENT_INCREMENT, XML_INDENT_INCREMENT, writer);

			// Write end tag of document element
			writer.write(XML_DOCUMENT_END_TAG);

			// Return text
			return writer.toString();
		}
		catch (IOException e)
		{
			throw new BaseException(ErrorMsg.ERROR_GENERATING_XML_TEXT, e);
		}
	}

	//------------------------------------------------------------------

	/**
	 * Writes the specified text to a file at the specified file-system location.  The text is encoded as UTF-8.
	 *
	 * @param  location
	 *           the location of the file to which {@code text} will be written.
	 * @param  text
	 *           the text that will be written to a file at {@code location}.
	 * @throws FileException
	 *           if an error occurs when writing the file.
	 */

	private static void writeTextFile(
		Path			location,
		CharSequence	text)
		throws FileException
	{
		// Write location of file to standard output stream
		System.out.println(WRITING_STR + " " + PathUtils.abs(location));

		// Write text to file
		try
		{
			Files.writeString(location, text);
		}
		catch (IOException e)
		{
			throw new FileException(ErrorMsg.ERROR_WRITING_FILE, e, location);
		}
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
	 * @return a status code: 0 for success, 1 for failure.
	 */

	private int run(
		String[]	args)
	{
		try
		{
			// Get location of output directory from command-line argument
			Path outDirectory = null;
			if (args.length < 1)
				System.out.println(NO_OUTPUT_DIRECTORY_STR);
			else
			{
				// Get location of output directory
				outDirectory = Path.of(substituteProperties(args[0]));

				// Test for output directory
				if (Files.exists(outDirectory, LinkOption.NOFOLLOW_LINKS)
						&& !Files.isDirectory(outDirectory, LinkOption.NOFOLLOW_LINKS))
					throw new FileException(ErrorMsg.NOT_A_DIRECTORY, outDirectory);
			}

			// Create tree that is precursor of JSON; generate JSON text from tree
			String inJsonText = JsonGenerator.builder().build().generate(createInputTree()).toString();

			// Write JSON text to file
			if (outDirectory != null)
			{
				// Create output directory
				try
				{
					Files.createDirectories(outDirectory);
				}
				catch (Exception e)
				{
					throw new FileException(ErrorMsg.FAILED_TO_CREATE_DIRECTORY, e, outDirectory);
				}

				// Write JSON text to file
				writeTextFile(outDirectory.resolve(JSON_INPUT_FILENAME), inJsonText);
			}

			// Create facade for elements of JSON-XML source tree.  This creates an XML document and document element.
			IElementFacade inElementFacade = new SimpleElementFacade(DOCUMENT_ELEMENT_NAME);

			// Parse JSON text to create tree of JSON-XML elements
			Element inRootElement = null;
			try
			{
				inRootElement = JsonParser.builder().elementFacade(inElementFacade).build().parseToXml(inJsonText);
			}
			catch (JsonParser.ParseException e)
			{
				throw new BaseException(ErrorMsg.ERROR_PARSING_JSON_TEXT, e);
			}

			// Add tree of JSON-XML elements to document element
			inRootElement.getOwnerDocument().getDocumentElement().appendChild(inRootElement);

			// Write input JSON-XML document to file
			if (outDirectory != null)
				writeTextFile(outDirectory.resolve(XML_INPUT_FILENAME), createXmlDocumentText(inRootElement));

			// Create facade for elements of JSON-XML output tree
			IElementFacade outElementFacade = new SimpleElementFacade(DOCUMENT_ELEMENT_NAME);

			// Transform JSON-XML tree with XSLT
			DOMResult result = new DOMResult(outElementFacade.createElement(DOCUMENT_ELEMENT_NAME));
			String xslPathname = "/" + getClass().getPackageName().replace('.', '/') + "/" + XSL_FILENAME;
			try (InputStream stream = getClass().getResourceAsStream(xslPathname))
			{
				Transformer transformer =
						BasicTransformerFactory.newInstance().newTransformer(new StreamSource(stream));
				transformer.transform(new DOMSource(inRootElement.getOwnerDocument()), result);
			}
			catch (Exception e)
			{
				throw new BaseException(ErrorMsg.ERROR_TRANSFORMING_XML_TREE, e);
			}

			// Validate output JSON-XML tree against XSD schema
			Element outRootElement = JsonXmlUtils.child((Element)result.getNode(), 0);
			JsonXmlValidator.validate(outRootElement, false);

			// Write output XML document to file
			if (outDirectory != null)
				writeTextFile(outDirectory.resolve(XML_OUTPUT_FILENAME), createXmlDocumentText(outRootElement));

			// Convert output XML to JSON text
			String outJsonText = JsonGeneratorXml.builder().elementFacade(outElementFacade).build()
					.generate(outRootElement)
					.toString();

			// Write output JSON text to file
			if (outDirectory != null)
				writeTextFile(outDirectory.resolve(JSON_OUTPUT_FILENAME), outJsonText);

			// Return status: success
			return 0;
		}
		catch (BaseException e)
		{
			// Write stack trace to standard error stream
			e.printStackTrace();

			// Return status: failure
			return 1;
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: FIELDS IN WHICH A NOBEL PRIZE IS AWARDED


	/**
	 * This is an enumeration of some of the fields in which Nobel Prizes are awarded.
	 */

	private enum Field
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/**
		 * The Nobel Prize in Chemistry
		 */
		CHEMISTRY,

		/**
		 * The Nobel Prize in Physics
		 */
		PHYSICS,

		/**
		 * The Nobel Prize in Literature
		 */
		LITERATURE;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for a field in which a Nobel Prize is awarded.
		 */

		private Field()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the key that is associated with this field.
		 *
		 * @return the key that is associated with this field.
		 */

		private String getKey()
		{
			return name().toLowerCase();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: PERSON


	/**
	 * Creates a new instance of a person.
	 *
	 * @param primaryName
	 *          the primary name of the person.
	 * @param secondaryName
	 *          the secondary name of the person.
	 */

	private record Person(
		String	primaryName,
		String	secondaryName)
	{ }

	//==================================================================


	// RECORD: AWARD OF A NOBEL PRIZE


	/**
	 * Creates a new instance of an award of a Nobel Prize.
	 *
	 * @param field
	 *          the field in which the prize was awarded.
	 * @param year
	 *          the year of the award.
	 * @param winner
	 *          the person to whom the prize was awarded.
	 */

	private record Award(
		Field	field,
		int		year,
		Person	winner)
	{ }

	//==================================================================

}

//----------------------------------------------------------------------
