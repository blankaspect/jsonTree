/*====================================================================*\

JsonUtils.java

Class: JSON-related utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

//----------------------------------------------------------------------


// CLASS: JSON-RELATED UTILITY METHODS


/**
 * This class contains utility methods that relate to JSON.
 */

public class JsonUtils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The name of the UTF-8 character encoding in the JDK. */
	private static final	String	ENCODING_NAME_UTF8	= "UTF-8";

	/** The string that is prefixed to the name of a temporary file. */
	private static final	String	TEMP_FILE_PREFIX	= "_$_";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private JsonUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Reads the specified file, parses its content as JSON text and returns the resulting JSON value.
	 *
	 * @param  file
	 *           the file that will be read and parsed as JSON text.
	 * @return the JSON value that results from parsing the content of <i>file</i>, if the file contains valid JSON
	 *         text.
	 * @throws IOException
	 *           if an error occurred when reading the file.
	 * @throws JsonParser.ParseException
	 *           if an error occurred when parsing the content of the file.
	 */

	public static JsonValue readFile(Path file)
		throws IOException, JsonParser.ParseException
	{
		// Read file
		String text = new String(Files.readAllBytes(file), ENCODING_NAME_UTF8);

		// Parse JSON text and return resulting JSON value
		return new JsonParser().parse(text);
	}

	//------------------------------------------------------------------

	/**
	 * Writes the specified JSON value to the specified file as JSON text.  The text is generated by a new instance of
	 * {@link JsonGenerator} that has default values for mode, indent increment and maximum line length.
	 *
	 * @param  file
	 *           the file to which the JSON text of <i>value</i> will be written.
	 * @param  value
	 *           the JSON value whose JSON text will be written to <i>file</i>.
	 * @throws IOException
	 *           if an error occurred when writing the file.
	 */

	public static void writeFile(Path      file,
								 JsonValue value)
		throws IOException
	{
		writeFile(file, value, new JsonGenerator());
	}

	//------------------------------------------------------------------

	/**
	 * Writes the specified JSON value to the specified file as JSON text that is generated by the specified instance of
	 * {@link JsonGenerator}.
	 *
	 * @param  file
	 *           the file to which the JSON text of <i>value</i> will be written.
	 * @param  value
	 *           the JSON value whose JSON text will be written to <i>file</i>.
	 * @param  generator
	 *           the object that will generate the JSON text for <i>value</i>.
	 * @throws IOException
	 *           if an error occurred when writing the file.
	 */

	public static void writeFile(Path          file,
								 JsonValue     value,
								 JsonGenerator generator)
		throws IOException
	{
		Path tempFile = null;
		try
		{
			// Create parent directory
			Path parent = file.getParent();
			if (parent != null)
				Files.createDirectories(parent);

			// Create temporary file
			tempFile = Files.createTempFile(TEMP_FILE_PREFIX, null);

			// Convert JSON value to text
			String text = generator.generate(value);

			// Write JSON text to temporary file
			Files.write(tempFile, text.getBytes(ENCODING_NAME_UTF8));

			// Rename temporary file to output file
			Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);

			// Invalidate temporary file
			tempFile = null;
		}
		finally
		{
			// Delete temporary file
			if ((tempFile != null) && Files.exists(tempFile))
				Files.delete(tempFile);
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
