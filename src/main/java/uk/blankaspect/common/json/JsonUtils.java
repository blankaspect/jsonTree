/*====================================================================*\

JsonUtils.java

Class: JSON-related utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// IMPORTS


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import uk.blankaspect.common.basictree.AbstractNode;

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

	/** The string that is prefixed to the name of a temporary file. */
	private static final	String	TEMPORARY_FILENAME_PREFIX	= "_$_";

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
	 * Returns {@code true} if the specified {@linkplain AbstractNode node} represents a JSON value.
	 *
	 * @param  node
	 *           the node of interest.
	 * @return {@code true} if <i>node</i> represents a JSON value; {@code false} otherwise.
	 */

	public static boolean isJsonValue(AbstractNode node)
	{
		return node.getType().isAnyOf(JsonConstants.NODE_TYPES);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the specified {@linkplain AbstractNode node} represents a simple JSON value (ie, a null,
	 * a Boolean, a number or a string).
	 *
	 * @param  node
	 *           the node of interest.
	 * @return {@code true} if <i>node</i> represents a JSON null, Boolean, number or string; {@code false} otherwise.
	 */

	public static boolean isSimpleJsonValue(AbstractNode node)
	{
		return node.getType().isAnyOf(JsonConstants.SIMPLE_NODE_TYPES);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the specified {@linkplain AbstractNode node} represents a JSON container (ie, an array or
	 * object).
	 *
	 * @param  node
	 *           the node of interest.
	 * @return {@code true} if <i>node</i> represents a JSON array or object; {@code false} otherwise.
	 */

	public static boolean isJsonContainer(AbstractNode node)
	{
		return node.getType().isAnyOf(JsonConstants.CONTAINER_NODE_TYPES);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the specified sequence of characters is found in the specified file, which is expected to
	 * contain text with the UTF-8 character encoding.
	 *
	 * @param  file
	 *           the file that will be searched.
	 * @param  target
	 *           the sequence of characters that will be searched for in <i>file</i>.
	 * @return {@code true} if <i>file</i> contains <i>target</i>.
	 * @throws IOException
	 *           if an error occurred when reading the file.
	 */

	public static boolean containsText(Path         file,
									   CharSequence target)
		throws IOException
	{
		// Validate arguments
		if (file == null)
			throw new IllegalArgumentException("Null file");
		if (target == null)
			throw new IllegalArgumentException("Null target");

		// Open character stream on file and search for target in stream
		BufferedReader reader = null;
		try
		{
			// Open character stream on file
			reader = Files.newBufferedReader(file);

			// Search for target in character stream and return the result
			return containsText(reader, target);
		}
		finally
		{
			// Close stream
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					// ignore
				}
			}
		}
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the specified sequence of characters is found in the specified character stream.
	 *
	 * @param  inputStream
	 *           the character stream that will be searched.
	 * @param  target
	 *           the sequence of characters that will be searched for in <i>inputStream</i>.
	 * @return {@code true} if <i>target</i> is found in <i>inputStream</i>.
	 * @throws IOException
	 *           if an error occurred when reading from the input stream.
	 */

	public static boolean containsText(Reader       inputStream,
									   CharSequence target)
		throws IOException
	{
		// Validate arguments
		if (inputStream == null)
			throw new IllegalArgumentException("Null input stream");
		if (target == null)
			throw new IllegalArgumentException("Null target");

		// Get length of target
		int targetLength = target.length();

		// If target is empty, it is deemed to be found in any input
		if (targetLength == 0)
			return true;

		// Create array of target characters
		char[] targetChars = new char[targetLength];
		for (int i = 0; i < targetLength; i++)
			targetChars[i] = target.charAt(i);

		// Initialise circular buffer for characters from input stream
		char buffer[] = new char[targetLength];
		int index = 0;
		boolean full = false;

		// Read from input stream until end of input stream is reached or target is found
		while (true)
		{
			// Read next character from input stream
			int ch = inputStream.read();

			// If end of stream, stop
			if (ch < 0)
				break;

			// Add character to tail of buffer
			buffer[index++] = (char)ch;

			// If index is at end of buffer, wrap it around
			if (index >= targetLength)
			{
				index = 0;
				full = true;
			}

			// If buffer is full, compare it with target
			if (full)
			{
				// Initialise index to target characters
				int i = 0;

				// Case: head of buffer is at start of array
				if (index == 0)
				{
					// Compare buffer with target
					while (i < targetLength)
					{
						if (targetChars[i] != buffer[i++])
						{
							i = -1;
							break;
						}
					}
				}

				// Case: head of buffer is not at start of array
				else
				{
					// Compare front part of buffer, from head of buffer to end of array, with front part of target
					int j = index;
					int length = targetLength - index;
					while (i < length)
					{
						if (targetChars[i++] != buffer[j++])
						{
							i = -1;
							break;
						}
					}

					// If front parts matched, compare back part of buffer, from start of array to tail of buffer, with
					// back part of target
					if (i >= 0)
					{
						j = 0;
						while (i < targetLength)
						{
							if (targetChars[i++] != buffer[j++])
							{
								i = -1;
								break;
							}
						}
					}
				}

				// If buffer matched target, indicate target was found
				if (i == targetLength)
					return true;
			}
		}

		// Indicate target was not found
		return false;
	}

	//------------------------------------------------------------------

	/**
	 * Opens a character stream on the specified file, which is expected to contain text with the UTF-8 character
	 * encoding, parses the character stream as JSON text and returns the resulting JSON value.
	 *
	 * @param  file
	 *           the file whose content will be parsed as JSON text.
	 * @return the JSON value that results from parsing the content of <i>file</i>, if the file contains valid JSON
	 *         text.
	 * @throws IOException
	 *           if an error occurred when reading the file.
	 * @throws JsonParser.ParseException
	 *           if an error occurred when parsing the content of the file.
	 */

	public static AbstractNode readFile(Path file)
		throws IOException, JsonParser.ParseException
	{
		BufferedReader reader = null;
		try
		{
			// Open character stream on file
			reader = Files.newBufferedReader(file);

			// Parse character stream as JSON text and return the resulting JSON value
			return new JsonParser().parse(reader);
		}
		finally
		{
			// Close stream
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					// ignore
				}
			}
		}
	}

	//------------------------------------------------------------------

	/**
	 * Reads the specified file, which is expected to contain text with the UTF-8 character encoding, and returns the
	 * resulting text.
	 *
	 * @param  file
	 *           the file that will be read.
	 * @return the text content of <i>file</i>.
	 * @throws IOException
	 *           if an error occurred when reading the file.
	 */

	public static String readText(Path file)
		throws IOException
	{
		// Read the file; convert its content to text and return the text
		return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
	}

	//------------------------------------------------------------------

	/**
	 * Writes the specified JSON value to the specified file as JSON text.  The text is generated by a new instance of
	 * {@link JsonGenerator} that has default values for the mode, the <i>opening bracket on the same line</i> flag,
	 * the indent increment and the maximum line length.  The file is written to a new file in the parent directory of
	 * the specified file, and the new file is then renamed to the specified file.
	 *
	 * @param  file
	 *           the file to which the JSON text of <i>value</i> will be written.
	 * @param  value
	 *           the JSON value whose JSON text will be written to <i>file</i>.
	 * @throws IOException
	 *           if an error occurred when writing the file.
	 */

	public static void writeFile(Path         file,
								 AbstractNode value)
		throws IOException
	{
		writeFile(file, value, new JsonGenerator());
	}

	//------------------------------------------------------------------

	/**
	 * Writes the specified JSON value to the specified file as JSON text that is generated by the specified instance of
	 * {@link JsonGenerator}.  The file is written to a new file in the parent directory of the specified file, and the
	 * new file is then renamed to the specified file.
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
								 AbstractNode  value,
								 JsonGenerator generator)
		throws IOException
	{
		// Convert the JSON value to text; write the text to the file
		writeText(file, generator.generate(value));
	}

	//------------------------------------------------------------------

	/**
	 * Writes the specified text to the specified file with the UTF-8 character encoding.  The file is written to a
	 * new file in the parent directory of the specified file, and the new file is then renamed to the specified file.
	 *
	 * @param  file
	 *           the file to which <i>text</i> will be written.
	 * @param  text
	 *           the text that will be written to <i>file</i>.
	 * @throws IOException
	 *           if an error occurred when writing the file.
	 */

	public static void writeText(Path   file,
								 String text)
		throws IOException
	{
		Path tempFile = null;
		try
		{
			// Create the parent directories of the output file
			Path parent = file.toAbsolutePath().getParent();
			if (parent != null)
				Files.createDirectories(parent);

			// Create a temporary file in the parent directory of the output file
			tempFile = Files.createTempFile(parent, TEMPORARY_FILENAME_PREFIX, null);

			// Write the text to the temporary file
			Files.write(tempFile, text.getBytes(StandardCharsets.UTF_8));

			// Rename the temporary file to the output file
			Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);

			// Invalidate the temporary file
			tempFile = null;
		}
		finally
		{
			// Delete the temporary file
			if ((tempFile != null) && Files.exists(tempFile))
				Files.delete(tempFile);
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
