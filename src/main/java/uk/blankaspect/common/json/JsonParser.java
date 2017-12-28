/*====================================================================*\

JsonParser.java

Class: JSON parser.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// IMPORTS


import java.math.BigDecimal;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import uk.blankaspect.common.basictree.AbstractNode;
import uk.blankaspect.common.basictree.BooleanNode;
import uk.blankaspect.common.basictree.DoubleNode;
import uk.blankaspect.common.basictree.IntNode;
import uk.blankaspect.common.basictree.LongNode;
import uk.blankaspect.common.basictree.NullNode;
import uk.blankaspect.common.basictree.StringNode;

import uk.blankaspect.common.indexedsub.IndexedSub;

//----------------------------------------------------------------------

/**
 * This class implements a parser that transforms JSON text into a tree of values that are represented by {@linkplain
 * AbstractNode nodes}.  The input text of the parser must conform to the JSON grammar as specified in the following
 * documents:
 * <ul>
 *   <li><a href="https://tools.ietf.org/html/rfc7159">IETF RFC7159</a></li>
 *   <li><a href="https://www.ecma-international.org/publications/standards/Ecma-404.htm">ECMA-404</a></li>
 * </ul>
 * <p>
 * The parser is implemented in the {@link #parse(CharSequence)} method as a <a
 * href="https://en.wikipedia.org/wiki/Finite-state_machine">finite-state machine</a> (FSM) that terminates with an
 * exception at the first error in the input text.  The FSM combines the lexical analysis and parsing of the input text
 * with the generation of the output (a tree of {@linkplain AbstractNode nodes} that represent JSON values).
 * </p>
 */

public class JsonParser
{
	/** Whitespace characters. */
	private static final	String	WHITESPACE	= "\t\n\r ";

	/** Structural characters. */
	private static final	char[]	STRUCTURAL_CHARS	= new char[]
	{
		JsonObject.START_CHAR,
		JsonObject.END_CHAR,
		JsonArray.START_CHAR,
		JsonArray.END_CHAR,
		JsonObject.NAME_VALUE_SEPARATOR_CHAR,
		JsonObject.PROPERTY_SEPARATOR_CHAR
	};

	/** Value terminators: the union of whitespace characters and structural characters. */
	private static final	String	VALUE_TERMINATORS	= WHITESPACE + new String(STRUCTURAL_CHARS);

	/** The prefix of a four-hex-digit Unicode representaton of a character. */
	private static final	String	UNICODE_PREFIX	= "U+";

	/** Miscellaneous strings. */
	private static final	String	CHARACTER_NOT_ALLOWED_STR	= "the character %1 at index %2 is not allowed.";
	private static final	String	ENDED_PREMATURELY_STR		= "it ended prematurely at index %1.";

	/** Mappings from characters in an escape sequence to their corresponding literal characters. */
	private static final	char[][]	ESCAPE_MAPPINGS	=
	{
		{ '\\', '\\' },
		{ '\"', '\"' },
		{ '/',  '/' },
		{ 'b',  '\b' },
		{ 't',  '\t' },
		{ 'n',  '\n' },
		{ 'f',  '\f' },
		{ 'r',  '\r' }
	};

	/** The states of the parser. */
	private enum State
	{
		VALUE_START,
		VALUE_END,
		LITERAL_VALUE,
		NUMBER_VALUE,
		STRING_VALUE,
		PROPERTY_START,
		PROPERTY_NAME_START,
		PROPERTY_NAME,
		PROPERTY_NAME_END,
		PROPERTY_END,
		ARRAY_ELEMENT_START,
		ARRAY_ELEMENT_END,
		DONE
	}

	/** The states of the number validator. */
	private enum NumberState
	{
		INTEGER_PART_SIGN,
		INTEGER_PART_FIRST_DIGIT,
		INTEGER_PART_DIGITS,
		FRACTION_PART_FIRST_DIGIT,
		FRACTION_PART_DIGITS,
		EXPONENT_SIGN,
		EXPONENT_FIRST_DIGIT,
		EXPONENT_DIGITS,
		DONE
	}

	/** Error messages. */
	private interface ErrorMsg
	{
		String	PREMATURE_END_OF_TEXT		= "The input text ended prematurely.";
		String	EXTRANEOUS_TEXT				= "There is extraneous text after the JSON value.";
		String	VALUE_EXPECTED				= "A value was expected.";
		String	PROPERTY_NAME_EXPECTED		= "A property name was expected.";
		String	NAME_SEPARATOR_EXPECTED		= "A name separator was expected.";
		String	END_OF_OBJECT_EXPECTED		= "An end-of-object character was expected.";
		String	ARRAY_ELEMENT_EXPECTED		= "An array element was expected.";
		String	END_OF_ARRAY_EXPECTED		= "An end-of-array character was expected.";
		String	ILLEGAL_CHARACTER_IN_STRING	= "The character '%1' is not allowed in a string.";
		String	ILLEGAL_VALUE				= "The value is illegal.";
		String	ILLEGAL_ESCAPE_SEQUENCE		= "The escape sequence '%1' is illegal.";
		String	DUPLICATE_PROPERTY_NAME		= "The object has more than one property with the name '%1'.";
		String	INVALID_NUMBER				= "The number is not valid";
		String	TOO_LARGE_FOR_INTEGER		= "The number is too large for an integer.";
	}

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: PARSE EXCEPTION


	/**
	 * This class implements an exception that is thrown if an error occurs when parsing JSON text.
	 */

	public static class ParseException
		extends Exception
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an exception with the specified message, line index, column index and optional
		 * substitution text.
		 *
		 * @param message
		 *          the message of the exception.
		 * @param lineIndex
		 *          the zero-based index of the line at which the exception occurred.
		 * @param columnIndex
		 *          the zero-based index of the column at which the exception occurred.
		 * @param substitutionSeqs
		 *          the text that will replace placeholders in <i>message</i>; see {@link IndexedSub#sub(String,
		 *          CharSequence...)}.
		 */

		private ParseException(String          message,
							   int             lineIndex,
							   int             columnIndex,
							   CharSequence... substitutionSeqs)
		{
			// Call superclass constructor
			super("(" + (lineIndex + 1) + ", " + (columnIndex + 1) + "): " + IndexedSub.sub(message, substitutionSeqs));

			// Initialise instance fields
			this.lineIndex = lineIndex;
			this.columnIndex = columnIndex;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the zero-based index of the line of the input text at which the exception occurred.
		 *
		 * @return the zero-based index of the line of the input text at which the exception occurred.
		 * @see    #getColumnIndex()
		 */

		public int getLineIndex()
		{
			return lineIndex;
		}

		//--------------------------------------------------------------

		/**
		 * Returns the zero-based index of the column of the input text at which the exception occurred.
		 *
		 * @return the zero-based index of the column of the input text at which the exception occurred.
		 * @see    #getLineIndex()
		 */

		public int getColumnIndex()
		{
			return columnIndex;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		/** The zero-based index of the line of the input text at which the exception occurred. */
		private	int	lineIndex;

		/** The zero-based index of the column of the input text at which the exception occurred. */
		private	int	columnIndex;

	}

	//==================================================================


	// CLASS: JSON OBJECT PROPERTY NAME


	/**
	 * This class encapsulates the name of a property of a JSON object and its location in the input text.
	 */

	private static class PropertyName
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a property-name object.
		 *
		 * @param name
		 *          the name of a property of a JSON object.
		 * @param index
		 *          the index of the property name in the input text.
		 * @param lineIndex
		 *          the index of the line containing the property name in the input text.
		 */

		private PropertyName(String name,
							 int    index,
							 int    lineIndex)
		{
			// Initialise instance fields
			this.name = name;
			this.index = index;
			this.lineIndex = lineIndex;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		/** The name of a property of a JSON object. */
		private	String	name;

		/** The index of the property name in the input text. */
		private	int		index;

		/** The index of the line containing the property name in the input text. */
		private	int		lineIndex;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON parser.
	 */

	public JsonParser()
	{
		// Initialise instance fields
		tokenBuffer = new StringBuilder();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if the specified character is whitespace.
	 *
	 * @param  ch
	 *           the character of interest.
	 * @return {@code true} if <i>ch</i> is whitespace.
	 */

	private static boolean isWhitespace(char ch)
	{
		return (WHITESPACE.indexOf(ch) >= 0);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the specified character is a value terminator (ie, either whitespace or a structural
	 * character).
	 *
	 * @param  ch
	 *           the character of interest.
	 * @return {@code true} if <i>ch</i> is whitespace or a structural character.
	 */

	private static boolean isValueTerminator(char ch)
	{
		return (VALUE_TERMINATORS.indexOf(ch) >= 0);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Sets or clears the flag that determines whether a JSON number that is deemed to be an integer but is too large to
	 * be stored as a signed 64-bit integer (ie, a {@code long}) will be stored as a double-precision floating-point
	 * number (ie, a {@code double}).
	 *
	 * @param storeExcessiveIntegerAsFP
	 *          if {@code true} a JSON number that is deemed to be an integer but is too large for a {@code long} will
	 *          be stored as a {@code double}.
	 */

	public void setStoreExcessiveIntegerAsFP(boolean storeExcessiveIntegerAsFP)
	{
		this.storeExcessiveIntegerAsFP = storeExcessiveIntegerAsFP;
	}

	//------------------------------------------------------------------

	/**
	 * Parses the specified text and, if it conforms to the JSON grammar, transforms it into a tree of {@linkplain
	 * AbstractNode nodes} that represent JSON values and returns the root of the tree.
	 *
	 * @param  text
	 *           the text that will be parsed as JSON text.
	 * @return the tree of JSON values that was created from parsing <i>text</i>.
	 * @throws ParseException
	 *           if an error occurred when parsing the input text.
	 */

	public AbstractNode parse(CharSequence text)
		throws ParseException
	{
		// Reset instance fields
		index = 0;
		lineIndex = 0;
		lineStartIndex = 0;
		tokenIndex = 0;
		tokenBuffer.setLength(0);

		// Initialise local variables
		Deque<PropertyName> propertyNameStack = new ArrayDeque<>();
		int propertyIndex = 0;
		int propertyLineIndex = 0;
		AbstractNode value = null;
		State state = State.VALUE_START;

		// Parse text
		while (state != State.DONE)
		{
			// Set flag to indicate end of input text
			boolean endOfText = (index >= text.length());

			// Get next character of input text
			char ch = endOfText ? ' ' : text.charAt(index++);

			// Execute finite-state machine
			switch (state)
			{
				//----  Start of JSON value
				case VALUE_START:
				{
					// Character is whitespace ...
					if (isWhitespace(ch))
					{
						// If end of input text, test whether parsing is complete ...
						if (endOfText)
						{
							// If current value is not root, input text has ended prematurely ...
							if (!value.isRoot())
								throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex,
														 index - lineStartIndex);

							// ... otherwise, parsing is complete
							state = State.DONE;
						}

						// ... otherwise, if character is LF, start new line
						else
							newLine(ch);
					}

					// Character is not whitespace ...
					else
					{
						// Update index of current token
						tokenIndex = index - 1;

						// Test for extraneous text after root value
						if ((value != null) && value.isRoot() && !value.isContainer())
							throw new ParseException(ErrorMsg.EXTRANEOUS_TEXT, lineIndex, tokenIndex - lineStartIndex);

						// Clear token buffer
						tokenBuffer.setLength(0);

						// Set next state according to character
						switch (ch)
						{
							case StringNode.START_CHAR:
								state = State.STRING_VALUE;
								break;

							case JsonObject.START_CHAR:
								value = new JsonObject(value);
								state = State.PROPERTY_START;
								break;

							case JsonArray.START_CHAR:
								value = new JsonArray(value);
								state = State.ARRAY_ELEMENT_START;
								break;

							case JsonObject.END_CHAR:
							case JsonArray.END_CHAR:
							case JsonObject.NAME_VALUE_SEPARATOR_CHAR:
							case JsonObject.PROPERTY_SEPARATOR_CHAR:
								throw new ParseException(ErrorMsg.VALUE_EXPECTED, lineIndex,
														 tokenIndex - lineStartIndex);

							default:
								// Rewind index to start of number or literal
								--index;

								// Set next state
								state = ((ch == '-') || ((ch >= '0') && (ch <= '9'))) ? State.NUMBER_VALUE
																					  : State.LITERAL_VALUE;
								break;
						}
					}
					break;
				}

				//----  End of JSON value
				case VALUE_END:
				{
					// Get parent of current value
					AbstractNode parent = value.getParent();

					// If current value has no parent (ie, it is the root value), move to next value (which should not
					// exist) ...
					if (parent == null)
						state = State.VALUE_START;

					// ... otherwise, if parent is object or array, add value to parent
					else
					{
						switch (parent.getKind())
						{
							case LIST:
								// Add element to its parent array
								((JsonArray)parent).addElement(value);

								// Set next state
								state = State.ARRAY_ELEMENT_END;
								break;

							case MAP:
							{
								// Cast parent to JSON object value
								JsonObject object = (JsonObject)parent;

								// Get name of current property from stack
								PropertyName propertyName = propertyNameStack.removeFirst();

								// Test for duplicate property name
								if (object.hasKey(propertyName.name))
									throw new ParseException(ErrorMsg.DUPLICATE_PROPERTY_NAME, propertyName.lineIndex,
															 propertyName.index - lineStartIndex, propertyName.name);

								// Add property to its parent object value
								object.addPair(propertyName.name, value);

								// Set next state
								state = State.PROPERTY_END;
								break;
							}

							default:
								throw new RuntimeException("Unexpected error: invalid parent");
						}

						// Rewind index to property/element separator or object/array terminator
						--index;

						// Set current value to previous parent
						value = parent;
					}
					break;
				}

				//----  JSON literal value (null or Boolean)
				case LITERAL_VALUE:
				{
					// If end of current token, set new null value or Boolean value according to token ...
					if (isValueTerminator(ch))
					{
						switch (tokenBuffer.toString())
						{
							case NullNode.VALUE:
								value = new NullNode(value);
								break;

							case BooleanNode.VALUE_FALSE:
								value = new BooleanNode(value, false);
								break;

							case BooleanNode.VALUE_TRUE:
								value = new BooleanNode(value, true);
								break;

							default:
								throw new ParseException(ErrorMsg.ILLEGAL_VALUE, lineIndex,
														 tokenIndex - lineStartIndex);
						}

						// Rewind index to value terminator
						--index;

						// Set next state
						state = State.VALUE_END;
					}

					// ... otherwise, append character to current token
					else
						tokenBuffer.append(ch);
					break;
				}

				//----  JSON number value
				case NUMBER_VALUE:
				{
					// Test for premature end of input text
					if (endOfText)
						throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex, index - lineStartIndex);

					try
					{
						// Rewind index to start of number
						--index;

						// Validate number; put valid number in token buffer
						validateNumber(text);

						// Parse number by creating new instance of BigDecimal from token
						String numberStr = tokenBuffer.toString();
						BigDecimal number = new BigDecimal(numberStr);

						// If number is integer, create JSON number of smallest type ...
						if (numberStr.indexOf('.') < 0)
						{
							try
							{
								value = new IntNode(value, number.intValueExact());
							}
							catch (ArithmeticException e)
							{
								try
								{
									value = new LongNode(value, number.longValueExact());
								}
								catch (ArithmeticException e0)
								{
									if (!storeExcessiveIntegerAsFP)
										throw new ParseException(ErrorMsg.TOO_LARGE_FOR_INTEGER, lineIndex,
																 tokenIndex - lineStartIndex);
									value = new DoubleNode(value, number.doubleValue());
								}
							}
						}

						// ... otherwise, create JSON number for double-precision FP
						else
							value = new DoubleNode(value, number.doubleValue());

						// Rewind index to terminator
						--index;

						// Set next state
						state = State.VALUE_END;
					}
					catch (NumberFormatException e)
					{
						String causeMessage = e.getMessage();
						throw new ParseException(((causeMessage == null) || causeMessage.isEmpty())
															? ErrorMsg.INVALID_NUMBER + "."
															: ErrorMsg.INVALID_NUMBER + ": " + causeMessage,
												 lineIndex, tokenIndex - lineStartIndex);
					}
					break;
				}

				//----  JSON string value
				case STRING_VALUE:
				{
					// Test for premature end of input text
					if (endOfText)
						throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex, index - lineStartIndex);

					// Parse string; put valid string in token buffer; if string is valid, create JSON string value
					if (parseString(text, ch))
					{
						// Create JSON string value from token
						value = new StringNode(value, tokenBuffer.toString());

						// Set next state
						state = State.VALUE_END;
					}
					break;
				}

				//----  Start of property of JSON object
				case PROPERTY_START:
				{
					// Test for premature end of input text
					if (endOfText)
						throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex, index - lineStartIndex);

					// Character is whitespace ...
					if (isWhitespace(ch))
					{
						// If character is LF, start new line
						newLine(ch);
					}

					// Character is not whitespace ...
					else
					{
						// If end-of-object character, empty object has ended ...
						if (ch == JsonObject.END_CHAR)
							state = State.VALUE_END;

						// ... otherwise, expect another property
						else
						{
							// Rewind index to start of property
							--index;

							// Set next state
							state = State.PROPERTY_NAME_START;
						}
					}
					break;
				}

				//----  Start of name of property of JSON object
				case PROPERTY_NAME_START:
				{
					// Test for premature end of input text
					if (endOfText)
						throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex, index - lineStartIndex);

					// Character is whitespace ...
					if (isWhitespace(ch))
					{
						// If character is LF, start new line
						newLine(ch);
					}

					// Character is not whitespace ...
					else
					{
						// Update index of current token
						tokenIndex = index - 1;

						// Test for start of property name
						if (ch != StringNode.START_CHAR)
							throw new ParseException(ErrorMsg.PROPERTY_NAME_EXPECTED, lineIndex,
													 tokenIndex - lineStartIndex);

						// Update property variables
						propertyIndex = tokenIndex;
						propertyLineIndex = lineIndex;

						// Clear token buffer
						tokenBuffer.setLength(0);

						// Set next state
						state = State.PROPERTY_NAME;
					}
					break;
				}

				//----  Name of property of JSON object
				case PROPERTY_NAME:
				{
					// Test for premature end of input text
					if (endOfText)
						throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex, index - lineStartIndex);

					// Parse property name; put valid property name in token buffer; if property name is valid, put it
					// on stack
					if (parseString(text, ch))
					{
						// Create property name and put it on stack
						propertyNameStack.addFirst(new PropertyName(tokenBuffer.toString(), propertyIndex,
																	propertyLineIndex));

						// Set next state
						state = State.PROPERTY_NAME_END;
					}
					break;
				}

				//----  End of name of property of JSON object
				case PROPERTY_NAME_END:
				{
					// Test for premature end of input text
					if (endOfText)
						throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex, index - lineStartIndex);

					// Character is whitespace ...
					if (isWhitespace(ch))
					{
						// If character is LF, start new line
						newLine(ch);
					}

					// Character is not whitespace ...
					else
					{
						// Test for property-name separator
						if (ch != JsonObject.NAME_VALUE_SEPARATOR_CHAR)
							throw new ParseException(ErrorMsg.NAME_SEPARATOR_EXPECTED, lineIndex,
													 tokenIndex - lineStartIndex);

						// Set next state
						state = State.VALUE_START;
					}

					break;
				}

				//----  End of property of JSON object
				case PROPERTY_END:
				{
					// Test for premature end of input text
					if (endOfText)
						throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex, index - lineStartIndex);

					// Character is whitespace ...
					if (isWhitespace(ch))
					{
						// If character is LF, start new line
						newLine(ch);
					}

					// Character is not whitespace ...
					else
					{
						// Update index of current token
						tokenIndex = index - 1;

						// Set next state according to current character
						switch (ch)
						{
							case JsonObject.PROPERTY_SEPARATOR_CHAR:
								state = State.PROPERTY_NAME_START;
								break;

							case JsonObject.END_CHAR:
								state = State.VALUE_END;
								break;

							default:
								throw new ParseException(ErrorMsg.END_OF_OBJECT_EXPECTED, lineIndex,
														 tokenIndex - lineStartIndex);
						}
					}
					break;
				}

				//----  Start of element of JSON array
				case ARRAY_ELEMENT_START:
				{
					// Test for premature end of input text
					if (endOfText)
						throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex, index - lineStartIndex);

					// Character is whitespace ...
					if (isWhitespace(ch))
					{
						// If character is LF, start new line
						newLine(ch);
					}

					// Character is not whitespace ...
					else
					{
						// If end-of-array character, array has ended ...
						if (ch == JsonArray.END_CHAR)
						{
							// Test for empty array
							if (!((JsonArray)value).isEmpty())
								throw new ParseException(ErrorMsg.ARRAY_ELEMENT_EXPECTED, lineIndex,
														 index - 1 - lineStartIndex);

							// Set next state
							state = State.VALUE_END;
						}

						// ... otherwise, expect another element
						else
						{
							// Rewind index to start of element
							--index;

							// Set next state
							state = State.VALUE_START;
						}
					}
					break;
				}

				//----  End of element of JSON array
				case ARRAY_ELEMENT_END:
				{
					// Test for premature end of input text
					if (endOfText)
						throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex, index - lineStartIndex);

					// Character is whitespace ...
					if (isWhitespace(ch))
					{
						// If character is LF, start new line
						newLine(ch);
					}

					// Character is not whitespace ...
					else
					{
						// Update index of current token
						tokenIndex = index - 1;

						// Set next state according to current character
						switch (ch)
						{
							case JsonArray.ELEMENT_SEPARATOR_CHAR:
								state = State.ARRAY_ELEMENT_START;
								break;

							case JsonArray.END_CHAR:
								state = State.VALUE_END;
								break;

							default:
								throw new ParseException(ErrorMsg.END_OF_ARRAY_EXPECTED, lineIndex,
														 tokenIndex - lineStartIndex);
						}
					}
					break;
				}

				//----  Parsing completed successfully
				case DONE:
					// do nothing
					break;
			}
		}

		return value;
	}

	//------------------------------------------------------------------

	/**
	 * Increments the index of the current line in the input text and resets the index of the start of the current line
	 * in the input text if the specified character (from the input text) is a line feed (U+000A).
	 *
	 * @param ch
	 *          the next character from the input text.
	 */

	private void newLine(char ch)
	{
		if (ch == '\n')
		{
			++lineIndex;
			lineStartIndex = index;
		}
	}

	//------------------------------------------------------------------

	/**
	 * Throws a {@link ParseException} when the validation of a JSON number with {@link #validateNumber(CharSequence)}
	 * fails at the specified character.  The detail message of the exception contains a reference to the index of the
	 * character at which validation failed.
	 *
	 * @param  ch
	 *           the character that caused the validation of a number to fail.
	 * @throws ParseException
	 */

	private void throwNumberException(char ch)
		throws ParseException
	{
		// Get string representation of index of character
		String indexStr = Integer.toString(index - 1 - tokenIndex);

		// Initialise secondary message
		String message = null;

		// If character is terminator, secondary message is 'ended prematurely' ...
		if (isValueTerminator(ch))
			message = IndexedSub.sub(ENDED_PREMATURELY_STR, indexStr);

		// ... otherwise, secondary message is 'character is not allowed'
		else
		{
			String charStr = ((ch < '\u0020') || (ch > '\u007E')) ? UNICODE_PREFIX + StringNode.charToUnicodeHex(ch)
																  : "'" + Character.toString(ch) + "'";
			message = IndexedSub.sub(CHARACTER_NOT_ALLOWED_STR, charStr, indexStr);
		}

		// Throw exception
		throw new ParseException(ErrorMsg.INVALID_NUMBER + ": " + message, lineIndex, tokenIndex - lineStartIndex);
	}

	//------------------------------------------------------------------

	/**
	 * Validates a JSON number at the {@linkplain #index current index} within the specified text.  This method only
	 * checks that a number conforms to the JSON grammar; the number is subsequently parsed by calling {@link
	 * BigDecimal#BigDecimal(String)}.
	 * <p>
	 * The use of a finite-state machine to validate a JSON number is preferred to a regular expression because it is
	 * faster.
	 * </p>
	 *
	 * @param  text
	 *           the text that contains a JSON representation of a number at the current index.
	 * @throws ParseException
	 *           if the text at the current index is not a valid JSON representation of a number.
	 */

	private void validateNumber(CharSequence text)
		throws ParseException
	{
		NumberState state = NumberState.INTEGER_PART_SIGN;
		while (state != NumberState.DONE)
		{
			// Get next character of input text
			char ch = (index < text.length()) ? text.charAt(index++) : ' ';

			// Execute finite-state machine
			switch (state)
			{
				//----  Sign of integer part
				case INTEGER_PART_SIGN:
				{
					// If minus sign, append it to current token ...
					if (ch == '-')
						tokenBuffer.append(ch);

					// ... otherwise, rewind index to first character of number
					else
						--index;

					// Set next state
					state = NumberState.INTEGER_PART_FIRST_DIGIT;
					break;
				}

				//----  First digit of integer part
				case INTEGER_PART_FIRST_DIGIT:
				{
					// Test for decimal digit
					if ((ch < '0') || (ch > '9'))
						throwNumberException(ch);

					// Append digit to current token
					tokenBuffer.append(ch);

					// Set next state
					state = NumberState.INTEGER_PART_DIGITS;
					break;
				}

				//----  Remaining digits of integer part
				case INTEGER_PART_DIGITS:
				{
					// If end of number, stop validation ...
					if (isValueTerminator(ch))
						state = NumberState.DONE;

					// ... otherwise, if decimal point, append it to token ...
					else if (ch == '.')
					{
						// Append decimal point to current token
						tokenBuffer.append(ch);

						// Set next state
						state = NumberState.FRACTION_PART_FIRST_DIGIT;
					}

					// ... otherwise, if exponent prefix, append it to token ...
					else if ((ch == 'E') || (ch == 'e'))
					{
						// Append exponent prefix to current token
						tokenBuffer.append(ch);

						// Set next state
						state = NumberState.EXPONENT_SIGN;
					}

					// ... otherwise, if decimal digit, process it ...
					else if ((ch >= '0') && (ch <= '9'))
					{
						switch (tokenBuffer.length())
						{
							// Positive number may not have leading zero
							case 1:
								if (tokenBuffer.charAt(0) == '0')
								{
									--index;
									throwNumberException('0');
								}
								break;

							// Negative number may not have leading zero
							case 2:
								if ((tokenBuffer.charAt(0) == '-') && (tokenBuffer.charAt(1) == '0'))
								{
									--index;
									throwNumberException('0');
								}
								break;

							default:
								// do nothing
								break;
						}

						// Append digit to current token
						tokenBuffer.append(ch);
					}

					// ... otherwise, throw an exception
					else
						throwNumberException(ch);

					break;
				}

				//----  First digit of fraction part
				case FRACTION_PART_FIRST_DIGIT:
				{
					// Test for decimal digit
					if ((ch < '0') || (ch > '9'))
						throwNumberException(ch);

					// Append digit to current token
					tokenBuffer.append(ch);

					// Set next state
					state = NumberState.FRACTION_PART_DIGITS;
					break;
				}

				//----  Remaining digits of fraction part
				case FRACTION_PART_DIGITS:
				{
					// If end of number, stop validation ...
					if (isValueTerminator(ch))
						state = NumberState.DONE;

					// ... otherwise, if exponent prefix, append it to token ...
					else if ((ch == 'E') || (ch == 'e'))
					{
						// Append exponent prefix to current token
						tokenBuffer.append(ch);

						// Set next state
						state = NumberState.EXPONENT_SIGN;
					}

					// ... otherwise, if decimal digit, append it to current token ...
					else if ((ch >= '0') && (ch <= '9'))
						tokenBuffer.append(ch);

					// ... otherwise, throw an exception
					else
						throwNumberException(ch);
					break;
				}

				//----  Sign of exponent
				case EXPONENT_SIGN:
				{
					// If sign of exponent, append it to token ...
					if ((ch == '-') || (ch == '+'))
						tokenBuffer.append(ch);

					// ... otherwise, rewind index to first digit of exponent
					else
						--index;

					// Set next state
					state = NumberState.EXPONENT_FIRST_DIGIT;
					break;
				}

				//----  First digit of exponent
				case EXPONENT_FIRST_DIGIT:
				{
					// Test for decimal digit
					if ((ch < '0') || (ch > '9'))
						throwNumberException(ch);

					// Append digit to current token
					tokenBuffer.append(ch);

					// Set next state
					state = NumberState.EXPONENT_DIGITS;
					break;
				}

				//----  Remaining digits of exponent
				case EXPONENT_DIGITS:
				{
					// If end of number, stop validation ...
					if (isValueTerminator(ch))
						state = NumberState.DONE;

					// ... otherwise, if decimal digit, append it to current token ...
					else if ((ch >= '0') && (ch <= '9'))
						tokenBuffer.append(ch);

					// ... otherwise, throw an exception
					else
						throwNumberException(ch);
					break;
				}

				//----  Validation completed successfully
				case DONE:
					// do nothing
					break;
			}
		}
	}

	//------------------------------------------------------------------

	/**
	 * Parses a JSON string at the {@linkplain #index current index} within the specified text, and sets the resulting
	 * string in the {@linkplain #tokenBuffer token buffer}.  This method is not called on the quotation mark (U+0022)
	 * at the start of the string.
	 *
	 * @param  text
	 *           the input text that contains a JSON representation of a string at the current index.
	 * @param  ch
	 *           the current character of the JSON string representation.
	 * @return {@code true} if the string has been parsed successfully, {@code false} if the end of the string has not
	 *         been reached.
	 * @throws ParseException
	 *           if an error occurred when parsing the JSON string.
	 */

	private boolean parseString(CharSequence text,
								char         ch)
		throws ParseException
	{
		// Test for end of string
		if (ch == StringNode.END_CHAR)
			return true;

		// Test for control character
		if (ch < '\u0020')
			throw new ParseException(ErrorMsg.ILLEGAL_CHARACTER_IN_STRING, lineIndex, tokenIndex - lineStartIndex,
									 UNICODE_PREFIX + StringNode.charToUnicodeHex(ch));

		// If character is escape prefix, parse escape sequence
		if (ch == StringNode.ESCAPE_PREFIX_CHAR)
		{
			// Test whether input text ends before first character of escape sequence after prefix
			if (index >= text.length())
				throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex, index - lineStartIndex);

			// Initialise index of start of escape sequence without prefix
			int startIndex = index;

			// Get first character of escape sequence after prefix
			ch = text.charAt(index++);

			// If escape sequence is Unicode ...
			if (ch == StringNode.UNICODE_ESCAPE_CHAR)
			{
				// Test whether input text ends before expected end of Unicode escape sequence
				if (index + StringNode.UNICODE_LENGTH >= text.length())
					throw new ParseException(ErrorMsg.PREMATURE_END_OF_TEXT, lineIndex, text.length() - lineStartIndex);

				// Increment index to end of Unicode escape sequence
				index += StringNode.UNICODE_LENGTH;
			}

			// Parse escape sequence
			try
			{
				// If Unicode escape sequence, parse it as such ...
				if (ch == StringNode.UNICODE_ESCAPE_CHAR)
					ch = (char)Integer.parseUnsignedInt(text.subSequence(startIndex + 1, index).toString(), 16);

				// ... otherwise, parse it as a non-Unicode escape sequence
				else
				{
					char ch0 = ch;
					char[] pair = Arrays.stream(ESCAPE_MAPPINGS)
										.filter(pair0 -> (ch0 == pair0[0]))
										.findFirst()
										.orElseThrow(() -> new IllegalArgumentException());
					ch = pair[1];
				}
			}
			catch (IllegalArgumentException e)
			{
				--startIndex;
				throw new ParseException(ErrorMsg.ILLEGAL_ESCAPE_SEQUENCE, lineIndex, startIndex - lineStartIndex,
										 text.subSequence(startIndex, index));
			}
		}

		// Append character to buffer
		tokenBuffer.append(ch);

		// Indicate that the end of the string has not been reached
		return false;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	/** Flag: if {@code true}, a JSON number that is deemed to be an integer but is too large to be stored as a
		{@linkplain Long signed 64-bit integer} will be stored as a {@linkplain Double double-precision floating-point
		number}. */
	private	boolean			storeExcessiveIntegerAsFP;

	/** The index of the current character in the input text. */
	private	int				index;

	/** The index of the current line in the input text. */
	private	int				lineIndex;

	/** The index of the start of the current line in the input text. */
	private	int				lineStartIndex;

	/** The index of the current token in the input text. */
	private	int				tokenIndex;

	/** A buffer for the current token. */
	private	StringBuilder	tokenBuffer;

}

//----------------------------------------------------------------------
