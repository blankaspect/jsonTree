/*====================================================================*\

JsonGenerator.java

Class: JSON generator.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Arrays;
import java.util.List;

//----------------------------------------------------------------------


// CLASS: JSON GENERATOR


/**
 * This class implements a generator of JSON text for a {@linkplain JsonValue JSON value}.  The generator operates in
 * one of several {@linkplain Mode <i>modes</i>}, which control the way in which whitespace is written between the
 * tokens of the JSON text.
 */

public class JsonGenerator
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/**
	 * This is an enumeration of the modes that control the way in which whitespace is written between the tokens of the
	 * JSON text.
	 */

	public enum Mode
	{
		/**
		 * JSON text is written on a single line with no space between tokens.
		 */
		DENSE,

		/**
		 * JSON text is written on a single line with a space between some tokens.
		 */
		COMPACT,

		/**
		 * JSON text may be written on multiple lines with a space between some tokens.  Compound values (array and
		 * object) are written on a single line in some cases:
		 * <ul>
		 *   <li>The opening and closing brackets and elements of an array are written on a single line if they all fit
		 *       on the line; otherwise, they are split over multiple lines, as necessary.</li>
		 *   <li>An object is written on a single line if it is empty or contains a single property that fits on the
		 *       line along with its opening and closing braces; otherwise, its properties and closing brace are each
		 *       written on a separate line, and its opening brace is written on a separate line if the <i>opening
		 *       bracket on the same line</i> flag is {@code false}.</li>
		 * </ul>
		 */
		NORMAL,

		/**
		 * JSON text may be written on multiple lines with a space between some tokens.  Compound values (array and
		 * object) are written on a single line in some cases:
		 * <ul>
		 *   <li>An empty array is written on a single line.  The elements and closing bracket of a non-empty array are
		 *       each written on a separate line, and the opening bracket is written on a separate line if the
		 *       <i>opening bracket on same line</i> flag is {@code false}.</li>
		 *   <li>An empty object is written on a single line.  The properties and closing brace of a non-empty object
		 *       are each written on a separate line, and the opening brace is written on a separate line if the
		 *       <i>opening bracket on the same line</i> flag is {@code false}.</li>
		 * </ul>
		 */
		EXPANDED
	}

	/** The default mode of a generator. */
	private static final	Mode	DEFAULT_MODE				= Mode.NORMAL;

	/** The default number of spaces by which indentation will be increased from one level to the next. */
	private static final	int		DEFAULT_INDENT_INCREMENT	= 2;

	/** The default maximum line length. */
	private static final	int		DEFAULT_MAX_LINE_LENGTH		= 80;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON generator with default values for mode ({@link Mode#NORMAL NORMAL}), <i>opening
	 * bracket on the same line</i> flag (false), indent increment (2) and maximum line length (80).
	 */

	public JsonGenerator()
	{
		// Call alternative constructor
		this(DEFAULT_MODE, false, DEFAULT_INDENT_INCREMENT, DEFAULT_MAX_LINE_LENGTH);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON generator with the specified mode.  Default values will be used for the
	 * <i>opening bracket on the same line</i> flag (false), indent increment (2) and maximum line length (80), which
	 * are relevant only in a multi-line mode ({@link Mode#NORMAL NORMAL} or {@link Mode#EXPANDED EXPANDED}).
	 *
	 * @param mode
	 *          the mode of the generator, which controls the way in which whitespace is written between the tokens of
	 *          the JSON text.
	 */

	public JsonGenerator(Mode mode)
	{
		// Call alternative constructor
		this(mode, false, DEFAULT_INDENT_INCREMENT, DEFAULT_MAX_LINE_LENGTH);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON generator with the specified mode and <i>opening bracket on the same line</i>
	 * flag.  Default values will be used for indent increment (2) and maximum line length (80), which are relevant only
	 * in a multi-line mode ({@link Mode#NORMAL NORMAL} or {@link Mode#EXPANDED EXPANDED}).
	 *
	 * @param mode
	 *          the mode of the generator, which controls the way in which whitespace is written between the tokens of
	 *          the JSON text.
	 * @param openingBracketOnSameLine
	 *          if {@code true}, the opening bracket of a JSON object or array will be written on the same line as the
	 *          non-whitespace text that precedes it.
	 */

	public JsonGenerator(Mode    mode,
						 boolean openingBracketOnSameLine)
	{
		// Call alternative constructor
		this(mode, openingBracketOnSameLine, DEFAULT_INDENT_INCREMENT, DEFAULT_MAX_LINE_LENGTH);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON generator with the specified mode, <i>opening bracket on the same line</i> flag,
	 * indent increment and maximum line length.
	 *
	 * @param mode
	 *          the mode of the generator, which controls the way in which whitespace is written between the tokens of
	 *          the JSON text.
	 * @param openingBracketOnSameLine
	 *          if {@code true}, the opening bracket of a JSON object or array will be written on the same line as the
	 *          non-whitespace text that precedes it.
	 * @param indentIncrement
	 *          the number of spaces by which indentation will be increased from one level to the next.  This parameter
	 *          has no effect in a single-line mode ({@link Mode#DENSE DENSE} or {@link Mode#COMPACT COMPACT}).
	 * @param maxLineLength
	 *          the maximum length of a line of JSON text.  This parameter has no effect in a single-line mode ({@link
	 *          Mode#DENSE DENSE} or {@link Mode#COMPACT COMPACT}).
	 */

	public JsonGenerator(Mode    mode,
						 boolean openingBracketOnSameLine,
						 int     indentIncrement,
						 int     maxLineLength)
	{
		// Initialise instance fields
		this.mode = mode;
		this.openingBracketOnSameLine = openingBracketOnSameLine;
		this.indentIncrement = indentIncrement;
		this.maxLineLength = maxLineLength;
		spaces = new char[0];
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Generates and returns JSON text for the specified {@linkplain JsonValue JSON value} in accordance with the
	 * properties of this generator (mode, indent increment and maximum line length).
	 *
	 * @param  value
	 *           the value for which JSON text will be generated.
	 * @return JSON text for <i>value</i>.
	 */

	public String generate(JsonValue value)
	{
		StringBuilder buffer = new StringBuilder(256);
		appendValue(value, 0, buffer);
		if (isMultilineMode())
			buffer.append('\n');
		return buffer.toString();
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this generator should write the JSON text for the specified {@linkplain JsonValue JSON
	 * value} on a single line.  This method always returns {@code true} for simple values (null, Boolean, number and
	 * string); for compound values (array and object), it applies a heuristic that takes into account the mode and
	 * maximum line length of this generator and the specified indentation.
	 *
	 * @param  value
	 *           the JSON value of interest.
	 * @param  indent
	 *           the number of spaces by which the JSON text is indented.
	 * @return {@code true} if this generator should write the JSON text for {@ode value} on a single line.
	 */

	private boolean isValueOnSingleLine(JsonValue value,
										int       indent)
	{
		boolean singleLine = true;
		if (isMultilineMode())
		{
			switch (value.getKind())
			{
				case NULL:
				case BOOLEAN:
				case NUMBER:
				case STRING:
					break;

				case OBJECT:
				{
					JsonObject object = (JsonObject)value;
					int numProperties = object.getNumProperties();
					singleLine = (numProperties == 0)
									|| ((mode != Mode.EXPANDED) && (numProperties == 1)
										&& isValueOnSingleLine(object.getProperties().values().iterator().next(),
															   indent + 2));
					break;
				}

				case ARRAY:
				{
					JsonArray array = (JsonArray)value;
					int numElements = array.getNumElements();
					if (((mode == Mode.EXPANDED) && (numElements > 0))
						|| array.getElements().stream().anyMatch(element -> !isValueOnSingleLine(element, indent + 2)))
						singleLine = false;
					else if (numElements > 1)
					{
						int lineLength = indent + 2 + 2 * numElements;
						for (int i = 0; i < numElements; i++)
						{
							lineLength += array.getElement(i).toString().length();
							if (lineLength > maxLineLength)
							{
								singleLine = false;
								break;
							}
						}
					}
					break;
				}
			}
		}
		return singleLine;
	}

	//------------------------------------------------------------------

	/**
	 * Generates the JSON text for the specified {@linkplain JsonValue JSON value} and appends it to the specified
	 * buffer.
	 *
	 * @param value
	 *          the JSON value for which JSON text will be generated and appended to <i>buffer</i>.
	 * @param indent
	 *          the number of spaces by which the JSON text is indented.
	 * @param buffer
	 *          the buffer to which the JSON text will be appended.
	 */

	private void appendValue(JsonValue     value,
							 int           indent,
							 StringBuilder buffer)
	{
		// Get indent of children of target value
		int childIndent = indent + indentIncrement;

		// Update array of spaces for indent; append indent
		if (isMultilineMode())
		{
			// Update array of spaces
			if (spaces.length < childIndent)
			{
				spaces = new char[childIndent];
				Arrays.fill(spaces, ' ');
			}

			// Append indent
			buffer.append(spaces, 0, indent);
		}

		// Append value
		switch (value.getKind())
		{
			case NULL:
			case BOOLEAN:
			case NUMBER:
			case STRING:
				buffer.append(value);
				break;

			case OBJECT:
			{
				// Get number of properties and names of properties
				JsonObject object = (JsonObject)value;
				int numProperties = object.getNumProperties();
				List<String> names = object.getNames();

				// If required, remove LF and indent before opening brace
				if (openingBracketOnSameLine)
					removeLineFeedAndIndent(buffer);

				// Append opening brace
				buffer.append(JsonObject.START_CHAR);

				// Properties of object are on a single line ...
				if (isValueOnSingleLine(object, indent))
				{
					// Append properties
					for (int i = 0; i < numProperties; i++)
					{
						// Append separator between properties
						if (i > 0)
							buffer.append(JsonObject.PROPERTY_SEPARATOR_CHAR);

						// Append space after separator
						if (mode != Mode.DENSE)
							buffer.append(' ');

						// Append name of property
						String name = names.get(i);
						buffer.append(JsonString.toJsonString(name));

						// Append separator between name and value
						buffer.append(JsonObject.NAME_SEPARATOR_CHAR);

						// Append space after separator
						if (mode != Mode.DENSE)
							buffer.append(' ');

						// Append value of property
						appendValue(object.getValue(name), 0, buffer);
					}

					// Append space before closing brace
					if (mode != Mode.DENSE)
						buffer.append(' ');
				}

				// Properties of object are not on a single line ...
				else
				{
					// Append LF after opening brace
					buffer.append('\n');

					// Append properties
					for (int i = 0; i < numProperties; i++)
					{
						// Get index of start of property
						int index = buffer.length();

						// Append indent before name of property
						buffer.append(spaces, 0, childIndent);

						// Append name of property
						String name = names.get(i);
						buffer.append(JsonString.toJsonString(name));

						// Append separator between name and value
						buffer.append(JsonObject.NAME_SEPARATOR_CHAR);

						// Get value of property
						JsonValue value0 = object.getValue(name);

						// If value of property is on single line, append it with a single space before it ...
						if (isValueOnSingleLine(value0, buffer.length() - index + 1))
							appendValue(value0, 1, buffer);

						// ... otherwise, append LF, indent and value of property
						else
						{
							buffer.append('\n');
							appendValue(value0, childIndent, buffer);
						}

						// Append separator between properties
						if (i < numProperties - 1)
							buffer.append(JsonObject.PROPERTY_SEPARATOR_CHAR);

						// Append LF after property
						buffer.append('\n');
					}

					// Append indent before closing brace
					buffer.append(spaces, 0, indent);
				}

				// Append closing brace
				buffer.append(JsonObject.END_CHAR);
				break;
			}

			case ARRAY:
			{
				// Get number of elements
				JsonArray array = (JsonArray)value;
				int numElements = array.getNumElements();

				// If required, remove LF and indent before opening bracket
				if (openingBracketOnSameLine)
					removeLineFeedAndIndent(buffer);

				// Append opening bracket
				buffer.append(JsonArray.START_CHAR);

				// Elements of array are on a single line ...
				if (isValueOnSingleLine(array, indent))
				{
					// Append elements
					for (int i = 0; i < numElements; i++)
					{
						// Append separator between elements
						if (i > 0)
							buffer.append(JsonArray.ELEMENT_SEPARATOR_CHAR);

						// Append space after separator
						if (mode != Mode.DENSE)
							buffer.append(' ');

						// Append element
						appendValue(array.getElement(i), 0, buffer);
					}

					// Append space before closing bracket
					if (mode != Mode.DENSE)
						buffer.append(' ');
				}

				// Elements of array are not on a single line ...
				else
				{
					// Append LF after opening brace
					buffer.append('\n');

					// Initialise line length
					int lineLength = 0;

					// Append elements
					for (int i = 0; i < numElements; i++)
					{
						// Get element
						JsonValue element = array.getElement(i);

						// Set 'more elements' flag
						boolean moreElements = (i < numElements - 1);

						// Element is a container ...
						if (element.isContainer())
						{
							// Append LF after previous element
							if (lineLength > 0)
								buffer.append('\n');

							// Append indent and element
							appendValue(element, childIndent, buffer);

							// Append separator between elements
							if (moreElements)
								buffer.append(JsonArray.ELEMENT_SEPARATOR_CHAR);

							// Append LF after element
							buffer.append('\n');

							// Reset line length
							lineLength = 0;
						}

						// Element is not a container ...
						else
						{
							// Convert element to string
							String elementStr = element.toString();

							// If not start of line, wrap line if necessary
							if (lineLength > 0)
							{
								// Increment line length past end of element
								lineLength += elementStr.length() + 1;
								if (moreElements)
									++lineLength;

								// If line would be too long, wrap it
								if (lineLength > maxLineLength)
								{
									buffer.append('\n');
									lineLength = 0;
								}
							}

							// If start of line ...
							if (lineLength == 0)
							{
								// Get index of start of element
								int index = buffer.length();

								// Append indent
								buffer.append(spaces, 0, childIndent);

								// Append element
								buffer.append(elementStr);

								// Append separator between elements
								if (moreElements)
									buffer.append(JsonArray.ELEMENT_SEPARATOR_CHAR);

								// If expanded mode, append LF after separator ...
								if (mode == Mode.EXPANDED)
									buffer.append('\n');

								// ... otherwise, increment line length
								else
									lineLength = buffer.length() - index;
							}

							// If not start of line ...
							else
							{
								// Append space before element
								buffer.append(' ');

								// Append element
								buffer.append(elementStr);

								// Append separator between elements
								if (moreElements)
									buffer.append(JsonArray.ELEMENT_SEPARATOR_CHAR);
							}
						}
					}

					// If not start of line, append LF
					if (lineLength > 0)
						buffer.append('\n');

					// Append indent before closing bracket
					buffer.append(spaces, 0, indent);
				}

				// Append closing bracket
				buffer.append(JsonArray.END_CHAR);
				break;
			}
		}
	}

	//------------------------------------------------------------------

	/**
	 * Removes a line feed and indent from the end of the specified buffer.

	 * @param buffer
	 *          the buffer from which a line feed and indent will be removed.
	 */

	private void removeLineFeedAndIndent(StringBuilder buffer)
	{
		int index = buffer.length();
		while (--index >= 0)
		{
			char ch = buffer.charAt(index);
			if (ch != ' ')
			{
				if (ch == '\n')
				{
					buffer.setLength(index);
					buffer.append(' ');
				}
				break;
			}
		}
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the current mode of this generator allows JSON text to be written on more than one line.
	 *
	 * @return {@code true} if the current mode of this generator allows JSON text to be written on more than one line;
	 *         {@code false} otherwise.
	 */

	private boolean isMultilineMode()
	{
		return (mode == Mode.NORMAL) || (mode == Mode.EXPANDED);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	/** The mode of this generator, which controls the way in which whitespace is written between the tokens of the JSON
		text. */
	private	Mode	mode;

	/** Flag: if {@code true}, the opening bracket of a JSON object or array is written on the same line as the
		non-whitespace text that precedes it. */
	private	boolean	openingBracketOnSameLine;

	/** The number of spaces by which indentation is increased from one level to the next. */
	private	int		indentIncrement;

	/** The maximum length of a line of JSON text. */
	private int		maxLineLength;

	/** An array of spaces that is used to indent line of generated JSON text. */
	private	char[]	spaces;

}

//----------------------------------------------------------------------
