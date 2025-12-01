/*====================================================================*\

JsonGeneratorXml.java

Class: generator of JSON text from JSON-XML.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.jsonxml;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import uk.blankaspect.common.basictree.StringNode;

import uk.blankaspect.common.json.JsonConstants;
import uk.blankaspect.common.json.JsonText;
import uk.blankaspect.common.json.NewLineBeforeLeftBracket;
import uk.blankaspect.common.json.OutputMode;

//----------------------------------------------------------------------


// CLASS: GENERATOR OF JSON TEXT FROM JSON-XML


/**
 * This class implements a generator that transforms a tree of values that are represented by {@linkplain Element XML
 * elements} into JSON text.  The generator operates in one of several {@linkplain OutputMode <i>output modes</i>},
 * which control the way in which whitespace is written between the tokens of the JSON text.
 */

public class JsonGeneratorXml
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The way in which whitespace is written between the tokens of the JSON text. */
	private	OutputMode					outputMode;

	/** The circumstance in which a new line is written before the opening bracket of a JSON array or the opening brace
		of a JSON object. */
	private	NewLineBeforeLeftBracket	newLineBeforeLeftBracket;

	/** The number of spaces by which indentation is increased from one level to the next. */
	private	int							indentIncrement;

	/** The maximum length of a line of JSON text. */
	private	int							maxLineLength;

	/** Flag: if {@code true}, the values of JSON strings and the names of the members of JSON objects will be escaped
		so that they contain only printable characters from the US-ASCII character encoding (ie, characters in the range
		U+0020 to U+007E inclusive). */
	private	boolean						printableAsciiOnly;

	/** The interface through which a JSON-XML element may be created and its attributes accessed. */
	private	IElementFacade				elementFacade;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON generator that is initialised from the state of the specified builder.
	 *
	 * @param builder
	 *          the builder from whose state the generator will be initialised.
	 */

	private JsonGeneratorXml(
		Builder	builder)
	{
		// Initialise instance variables
		outputMode = builder.outputMode;
		newLineBeforeLeftBracket = builder.newLineBeforeLeftBracket;
		indentIncrement = builder.indentIncrement;
		maxLineLength = builder.maxLineLength;
		printableAsciiOnly = builder.printableAsciiOnly;
		elementFacade = builder.elementFacade;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates and returns a new instance of a builder for a JSON generator.
	 *
	 * @return a new instance of a builder for a JSON generator.
	 */

	public static Builder builder()
	{
		return new Builder();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@linkplain JsonText.Token JSON token} that corresponds to the specified XML element.
	 *
	 * @param  element
	 *           the XML element whose corresponding JSON token is sought.
	 * @return the JSON token that corresponds to {@code element}, or {@code null} if there is no such token.
	 */

	public static JsonText.Token getToken(
		Element	element)
	{
		if (ElementKind.NULL.matches(element))
			return JsonText.Token.NULL_VALUE;
		if (ElementKind.BOOLEAN.matches(element))
			return JsonText.Token.BOOLEAN_VALUE;
		if (ElementKind.NUMBER.matches(element))
			return JsonText.Token.NUMBER_VALUE;
		if (ElementKind.STRING.matches(element))
			return JsonText.Token.STRING_VALUE;
		return null;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Generates and returns JSON text for the specified {@linkplain Element XML element} in accordance with the
	 * properties of this generator:
	 * <ul>
	 *   <li>output mode,</li>
	 *   <li><i>new line before left bracket</i> parameter,</li>
	 *   <li>indent increment,</li>
	 *   <li>maximum line length,</li>
	 *   <li><i>printable ASCII only</i> flag.</li>
	 * </ul>
	 *
	 * @param  element
	 *           the XML element for which JSON text will be generated.
	 * @return the JSON text that was generated for {@code element}.
	 * @throws UnrecognisedElementException
	 *           if any element in the tree whose root is {@code element} does not correspond to a JSON value.
	 */

	public JsonText generate(
		Element	element)
	{
		// Initialise JSON text
		JsonText text = new JsonText();

		// Append JSON text for value
		appendValue(element, 0, text);

		// Append new line in multi-line mode
		if (isMultilineMode())
			text.appendNewLine();

		// Return JSON text
		return text;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of the specified XML element.
	 *
	 * @param  element
	 *           the element for which a string representation is desired.
	 * @return a string representation of {@code element}.
	 */

	private String elementToString(
		Element	element)
	{
		return JsonXmlUtils.toJsonText(elementFacade, element, printableAsciiOnly);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the value of the <i>name</i> attribute of the specified XML element.
	 *
	 * @param  element
	 *           the element whose <i>name</i> attribute is desired.
	 * @return the value of the <i>name</i> attribute of {@code element}, or {@code null} if the element does not have
	 *         such an attribute.
	 */

	private String nameAttribute(
		Element	element)
	{
		String name = JsonXmlUtils.getName(elementFacade, element);
		return (name == null) ? null : StringNode.escapeAndQuote(name, printableAsciiOnly);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the maximum length that is available to append the JSON text of the specified XML element to the last
	 * line of the specified JSON text.
	 *
	 * @param  element
	 *           the XML element whose JSON text is considered for appending to the last line of {@code text}.
	 * @param  text
	 *           the JSON text whose last line would have the JSON text of {@code element} appended to it.
	 * @return the maximum length that is available to append the JSON text of {@code element} to the last line of
	 *         {@code text}.
	 */

	private int computeMaxLineLength(
		Element		element,
		JsonText	text)
	{
		int maxLength = maxLineLength - text.lastLineLength() - 2;
		if ((element.getParentNode() instanceof Element parent) && ElementKind.isCompound(parent))
		{
			List<Element> children = JsonXmlUtils.children(parent);
			if (children.get(children.size() - 1) == element)
				++maxLength;
		}
		return Math.max(0, maxLength);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this generator should write the JSON text for the specified XML element on a single line.
	 * This method always returns {@code true} in single-line output mode and for simple values (null, Boolean, number
	 * and string).  For compound values (array and object), it applies a heuristic that takes into account the output
	 * mode and the specified maximum length of a line of text.
	 *
	 * @param  element
	 *           the element of interest.
	 * @param  maxLength
	 *           the maximum length of a line of text.
	 * @return {@code true} if this generator should write the JSON text for {@code value} on a single line.
	 */

	private boolean isValueOnSingleLine(
		Element	element,
		int		maxLength)
	{
		// Always on a single line in single-line output mode
		if (!isMultilineMode())
			return true;

		// Assume a single line
		boolean singleLine = true;

		// Case: array
		if (ElementKind.ARRAY.matches(element))
		{
			int numChildren = JsonXmlUtils.countChildren(element);
			if (((outputMode == OutputMode.EXPANDED) && (numChildren > 0))
					|| JsonXmlUtils.children(element).stream()
							.anyMatch(child -> !isValueOnSingleLine(child, maxLength)))
				singleLine = false;
			else if (numChildren > 1)
			{
				int length = 0;
				for (int i = 0; i < numChildren; i++)
				{
					length += elementToString(JsonXmlUtils.child(element, i)).length() + 2;
					if (length > maxLength)
					{
						singleLine = false;
						break;
					}
				}
			}
		}

		// Case: object
		else if (ElementKind.OBJECT.matches(element))
		{
			singleLine = switch (JsonXmlUtils.countChildren(element))
			{
				case 0 -> true;
				case 1 ->
				{
					if (outputMode == OutputMode.EXPANDED)
						yield false;
					Element member = JsonXmlUtils.child(element, 0);
					String name = nameAttribute(member);
					yield isValueOnSingleLine(member, maxLength - ((name == null) ? 0 : name.length()));
				}
				default -> false;
			};
		}

		// Return result
		return singleLine;
	}

	//------------------------------------------------------------------

	/**
	 * Generates the JSON text for the specified {@linkplain Element XML element} and appends it to the specified
	 * text.
	 *
	 * @param  element
	 *           the XML element corresponding to a JSON value for which JSON text will be generated.
	 * @param  indent
	 *           the number of spaces by which a line of the JSON text is indented.
	 * @param  text
	 *           the JSON text to which the text that is generated by this method will be appended.
	 * @throws UnrecognisedElementException
	 *           if {@code value} does not correspond to a JSON value.
	 */

	private void appendValue(
		Element		element,
		int			indent,
		JsonText	text)
	{
		// Append indent
		if (isMultilineMode())
			text.appendSpaces(indent);

		// Case: array
		if (ElementKind.ARRAY.matches(element))
			appendArray(element, indent, text);

		// Case: object
		else if (ElementKind.OBJECT.matches(element))
			appendObject(element, indent, text);

		// Case: simple value
		else
		{
			// Get token
			JsonText.Token token = getToken(element);
			if (token == null)
				throw new UnrecognisedElementException(element);

			// Append JSON text of value
			text.append(elementToString(element), token);
		}
	}

	//------------------------------------------------------------------

	/**
	 * Generates the JSON text for the specified {@linkplain Element XML element} and appends it to the specified
	 * buffer.
	 *
	 * @param element
	 *          the XML element corresponding to a JSON array for which JSON text will be generated.
	 * @param indent
	 *          the number of spaces by which a line of the JSON text is indented.
	 * @param text
	 *          the JSON text to which the text that is generated by this method will be appended.
	 */

	private void appendArray(
		Element		element,
		int			indent,
		JsonText	text)
	{
		// Validate arguments
		if (!ElementKind.ARRAY.matches(element))
			throw new IllegalArgumentException("Not an array");

		// If required, remove line break and indent before opening bracket
		removeLineBreakAndIndent(text);

		// Append opening bracket
		text.append(JsonConstants.ARRAY_START_CHAR, JsonText.Token.ARRAY_DELIMITER);

		// Get number of array elements
		int numChildren = JsonXmlUtils.countChildren(element);

		// Case: elements of array are on a single line
		if (isValueOnSingleLine(element, computeMaxLineLength(element, text)))
		{
			// Append elements of array
			for (int i = 0; i < numChildren; i++)
			{
				// Append separator between elements
				if (i > 0)
					text.append(JsonConstants.ARRAY_ELEMENT_SEPARATOR_CHAR, JsonText.Token.ITEM_SEPARATOR);

				// Append space after separator
				if (outputMode != OutputMode.DENSE)
					text.appendSpace();

				// Append element
				appendValue(JsonXmlUtils.child(element, i), 0, text);
			}

			// Append space before closing bracket
			if (outputMode != OutputMode.DENSE)
				text.appendSpace();
		}

		// Case: elements of array are not on a single line
		else
		{
			// Append new line after opening brace
			text.appendNewLine();

			// Get indent of children of target value
			int childIndent = indent + indentIncrement;

			// Append elements
			for (int i = 0; i < numChildren; i++)
			{
				// Get element
				Element child = JsonXmlUtils.child(element, i);

				// Get length of current line
				int lineLength = text.lastLineLength();

				// Set 'more elements' flag
				boolean moreElements = (i < numChildren - 1);

				// Calculate maximum length of single line of text
				int maxLength = maxLineLength - ((lineLength == 0) ? childIndent : lineLength + 1) - 2;
				if (moreElements)
					--maxLength;

				// Case: element is on a single line
				if (isValueOnSingleLine(child, maxLength))
				{
					// Convert element to string
					String elementStr = elementToString(child);

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
							text.appendNewLine();
							lineLength = 0;
						}
					}

					// Append indent or space before element
					if (lineLength == 0)
						text.appendSpaces(childIndent);
					else
						text.appendSpace();

					// Append element
					text.append(elementStr, getToken(child));

					// Append separator between elements
					if (moreElements)
						text.append(JsonConstants.ARRAY_ELEMENT_SEPARATOR_CHAR, JsonText.Token.ITEM_SEPARATOR);

					// If expanded mode, append new line after element
					if (outputMode == OutputMode.EXPANDED)
						text.appendNewLine();
				}

				// Case: element is not on a single line
				else
				{
					// Append new line after previous element
					if (lineLength > 0)
						text.appendNewLine();

					// Append indent and element
					appendValue(child, childIndent, text);

					// Append separator between elements
					if (moreElements)
						text.append(JsonConstants.ARRAY_ELEMENT_SEPARATOR_CHAR, JsonText.Token.ITEM_SEPARATOR);

					// Append new line after element
					text.appendNewLine();
				}
			}

			// If not start of line, append new line
			if (text.lastLineLength() > 0)
				text.appendNewLine();

			// Append indent before closing bracket
			text.appendSpaces(indent);
		}

		// Append closing bracket
		text.append(JsonConstants.ARRAY_END_CHAR, JsonText.Token.ARRAY_DELIMITER);
	}

	//------------------------------------------------------------------

	/**
	 * Generates the JSON text for the specified {@linkplain Element XML element} and appends it to the specified
	 * text.
	 *
	 * @param  element
	 *           the XML element corresponding to a JSON object for which JSON text will be generated.
	 * @param  indent
	 *           the number of spaces by which a line of the JSON text is indented.
	 * @param  text
	 *           the JSON text to which the text that is generated by this method will be appended.
	 * @throws NoNameAttributeException
	 *           if a child of {@code element} (ie, a member of an &lt;object&gt; element) does not have a <i>name</i>
	 *           attribute.
	 */

	private void appendObject(
		Element		element,
		int			indent,
		JsonText	text)
	{
		// Validate arguments
		if (!ElementKind.OBJECT.matches(element))
			throw new IllegalArgumentException("Not an object");

		// If required, remove line break and indent before opening brace
		removeLineBreakAndIndent(text);

		// Append opening brace
		text.append(JsonConstants.OBJECT_START_CHAR, JsonText.Token.OBJECT_DELIMITER);

		// Case: members of object are on a single line
		if (isValueOnSingleLine(element, computeMaxLineLength(element, text)))
		{
			// Append members of object
			Iterator<Element> it = JsonXmlUtils.childIterator(element);
			while (it.hasNext())
			{
				// Append space after separator
				if (outputMode != OutputMode.DENSE)
					text.appendSpace();

				// Get member of object
				Element member = it.next();

				// Append name of member
				String name = nameAttribute(member);
				if (name == null)
					throw new NoNameAttributeException(member);
				text.append(name, JsonText.Token.OBJECT_MEMBER_NAME);

				// Append separator between name and value
				text.append(JsonConstants.OBJECT_NAME_VALUE_SEPARATOR_CHAR, JsonText.Token.OBJECT_NAME_VALUE_SEPARATOR);

				// Append space after separator
				if (outputMode != OutputMode.DENSE)
					text.appendSpace();

				// Append value of member
				appendValue(member, 0, text);

				// Append separator between members of object
				if (it.hasNext())
					text.append(JsonConstants.OBJECT_MEMBER_SEPARATOR_CHAR, JsonText.Token.ITEM_SEPARATOR);
			}

			// Append space before closing brace
			if (outputMode != OutputMode.DENSE)
				text.appendSpace();
		}

		// Case: members of object are not on a single line
		else
		{
			// Get indent of children of target value
			int childIndent = indent + indentIncrement;

			// Append new line after opening brace
			text.appendNewLine();

			// Append members of object
			Iterator<Element> it = JsonXmlUtils.childIterator(element);
			while (it.hasNext())
			{
				// Append indent before name of member
				text.appendSpaces(childIndent);

				// Get member
				Element member = it.next();

				// Append name of member
				String name = nameAttribute(member);
				if (name == null)
					throw new NoNameAttributeException(member);
				text.append(name, JsonText.Token.OBJECT_MEMBER_NAME);

				// Append separator between name and value
				text.append(JsonConstants.OBJECT_NAME_VALUE_SEPARATOR_CHAR, JsonText.Token.OBJECT_NAME_VALUE_SEPARATOR);

				// Calculate maximum length of single line of text
				int maxLength = maxLineLength - text.lastLineLength() - 3;
				if (it.hasNext())
					--maxLength;

				// If value of member is on single line, append it with a single space before it ...
				if (isValueOnSingleLine(member, maxLength))
					appendValue(member, 1, text);

				// ... otherwise, append new line, indent and value of member
				else
				{
					text.appendNewLine();
					appendValue(member, childIndent, text);
				}

				// Append separator between members of object
				if (it.hasNext())
					text.append(JsonConstants.OBJECT_MEMBER_SEPARATOR_CHAR, JsonText.Token.ITEM_SEPARATOR);

				// Append new line after member
				text.appendNewLine();
			}

			// Append indent before closing brace
			text.appendSpaces(indent);
		}

		// Append closing brace
		text.append(JsonConstants.OBJECT_END_CHAR, JsonText.Token.OBJECT_DELIMITER);
	}

	//------------------------------------------------------------------

	/**
	 * Removes a line break and indent from the end of the specified JSON text.
	 *
	 * @param text
	 *          the JSON text from which a line break and indent will be removed.
	 */

	private void removeLineBreakAndIndent(
		JsonText	text)
	{
		// Do nothing if there are fewer than two lines of JSON text or there should always be a new line before a left
		// bracket
		if ((text.lines().size() < 2) || (newLineBeforeLeftBracket == NewLineBeforeLeftBracket.ALWAYS))
			return;

		// Get last line of text
		JsonText.Line line = text.lastLine();

		// Consolidate adjacent spans of spaces
		line.normaliseSpans();

		// Get offset to start of last line
		int offset = line.offset();

		// If the last line contains only spaces and the penultimate line has an unwanted LF, replace the LF and the
		// spaces after it with a single space
		if ((line.spans().size() == 1) && (line.spans().get(0).token() == JsonText.Token.SPACE)
				&& ((newLineBeforeLeftBracket == NewLineBeforeLeftBracket.NEVER)
						|| ((offset > 1)
							&& (text.buffer().charAt(offset - 2) == JsonConstants.OBJECT_NAME_VALUE_SEPARATOR_CHAR))))
		{
			// Remove last line of spaces
			text.lines().remove(text.lines().size() - 1);

			// Get new last line
			line = text.lastLine();

			// Remove LF from last line
			line.spans().remove(line.spans().size() - 1);
			text.buffer().setLength(offset - 1);

			// Replace removed LF with space
			text.appendSpace();
		}
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the output mode of this generator allows JSON text to be written on more than one line.
	 *
	 * @return {@code true} if the output mode of this generator allows JSON text to be written on more than one line;
	 *         {@code false} otherwise.
	 */

	private boolean isMultilineMode()
	{
		return (outputMode == OutputMode.NORMAL) || (outputMode == OutputMode.EXPANDED);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: BUILDER FOR JSON GENERATOR


	/**
	 * This class implements a builder for a {@linkplain JsonGeneratorXml JSON generator}.
	 */

	public static class Builder
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The default output mode of a generator. */
		private static final	OutputMode	DEFAULT_OUTPUT_MODE	= OutputMode.NORMAL;

		/** The default circumstance in which a new line is written before the opening bracket of a JSON array or the
			opening brace of a JSON object. */
		private static final	NewLineBeforeLeftBracket	DEFAULT_NEW_LINE_BEFORE_LEFT_BRACKET	=
				NewLineBeforeLeftBracket.EXCEPT_AFTER_NAME;

		/** The default number of spaces by which indentation will be increased from one level to the next. */
		private static final	int		DEFAULT_INDENT_INCREMENT	= 2;

		/** The default maximum line length. */
		private static final	int		DEFAULT_MAX_LINE_LENGTH		= 96;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The way in which whitespace is written between the tokens of the JSON text. */
		private	OutputMode					outputMode;

		/** The circumstance in which a new line is written before the opening bracket of a JSON array or the opening
			brace of a JSON object. */
		private	NewLineBeforeLeftBracket	newLineBeforeLeftBracket;

		/** The number of spaces by which indentation is increased from one level to the next. */
		private	int							indentIncrement;

		/** The maximum length of a line of JSON text without wrapping. */
		private	int							maxLineLength;

		/** Flag: if {@code true}, the values of JSON strings and the names of the members of JSON objects will be
			escaped so that they contain only printable characters from the US-ASCII character encoding (ie, characters
			in the range U+0020 to U+007E inclusive). */
		private	boolean						printableAsciiOnly;

		/** The interface through which the <i>name</i> and <i>value</i> attributes of JSON-XML elements are
			accessed. */
		private	IElementFacade				elementFacade;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a builder for a {@linkplain JsonGeneratorXml JSON generator}.
		 */

		private Builder()
		{
			// Initialise instance variables
			outputMode = DEFAULT_OUTPUT_MODE;
			newLineBeforeLeftBracket = DEFAULT_NEW_LINE_BEFORE_LEFT_BRACKET;
			indentIncrement = DEFAULT_INDENT_INCREMENT;
			maxLineLength = DEFAULT_MAX_LINE_LENGTH;
			printableAsciiOnly = true;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Sets the way in which whitespace is written between the tokens of the JSON text.  The default value is {@link
		 * OutputMode#NORMAL NORMAL}.
		 *
		 * @param  outputMode
		 *           the way in which whitespace is written between the tokens of the JSON text.
		 * @return this builder.
		 */

		public Builder outputMode(
			OutputMode	outputMode)
		{
			// Update instance variable
			this.outputMode = outputMode;

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		/**
		 * Sets the circumstance in which a new line is written before the opening bracket of a JSON array or the
		 * opening brace of a JSON object.  The default value is {@link NewLineBeforeLeftBracket#EXCEPT_AFTER_NAME
		 * EXCEPT_AFTER_NAME}.
		 *
		 * @param  newLineBeforeLeftBracket
		 *           the circumstance in which a new line is written before the opening bracket of a JSON array or the
		 *           opening brace of a JSON object.
		 * @return this builder.
		 */

		public Builder newLineBeforeLeftBracket(
			NewLineBeforeLeftBracket	newLineBeforeLeftBracket)
		{
			// Update instance variable
			this.newLineBeforeLeftBracket = newLineBeforeLeftBracket;

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		/**
		 * Sets the number of spaces by which indentation is increased from one level to the next.  The default value is
		 * 2.
		 *
		 * @param  indentIncrement
		 *           the number of spaces by which indentation is increased from one level to the next.
		 * @return this builder.
		 */

		public Builder indentIncrement(
			int	indentIncrement)
		{
			// Update instance variable
			this.indentIncrement = indentIncrement;

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		/**
		 * Sets the maximum length of a line of JSON text without wrapping.  The default value is 96.
		 *
		 * @param  maxLineLength
		 *           the maximum length of a line of JSON text without wrapping.
		 * @return this builder.
		 */

		public Builder maxLineLength(
			int	maxLineLength)
		{
			// Update instance variable
			this.maxLineLength = maxLineLength;

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		/**
		 * Sets the flag that determines whether JSON string values and the names of the members of JSON objects will be
		 * escaped so that they contain only printable characters from the US-ASCII character encoding (ie, characters
		 * in the range U+0020 to U+007E inclusive).  The default value is {@code true}.
		 *
		 * @param  printableAsciiOnly
		 *           if {@code true}, the values of JSON strings and the names of the members of JSON objects will be
		 *           escaped so that they contain only printable characters from the US-ASCII character encoding.
		 * @return this builder.
		 */

		public Builder printableAsciiOnly(
			boolean	printableAsciiOnly)
		{
			// Update instance variable
			this.printableAsciiOnly = printableAsciiOnly;

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		/**
		 * Sets the interface through which the <i>name</i> and <i>value</i> attributes of JSON-XML elements are
		 * accessed.  The default value is {@code null} (the attributes are accessed directly).
		 *
		 * @param  elementFacade
		 *           the interface through which the <i>name</i> and <i>value</i> attributes of JSON-XML elements are
		 *           accessed.  If it is {@code null}, the attributes are accessed directly.
		 * @return this builder.
		 */

		public Builder elementFacade(
			IElementFacade	elementFacade)
		{
			// Update instance variable
			this.elementFacade = elementFacade;

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		/**
		 * Creates and returns a new instance of a JSON generator that is initialised from the state of this builder.
		 *
		 * @return a new instance of a JSON generator.
		 */

		public JsonGeneratorXml build()
		{
			return new JsonGeneratorXml(this);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ABSTRACT CLASS: ELEMENT EXCEPTION


	/**
	 * This is the abstract base class of an unchecked exception that is associated with an {@linkplain Element XML
	 * element}.
	 */

	public static abstract class ElementException
		extends RuntimeException
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The element that is associated with this exception. */
		private	Element	element;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an exception that is associated with the specified XML element.
		 *
		 * @param element
		 *          the XML element that will be associated with this exception.
		 */

		protected ElementException(
			Element	element)
		{
			// Initialise instance variables
			this.element = element;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public String toString()
		{
			return super.toString() + ((element == null) ? "" : " : element: " + element.getTagName());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the element that is associated with this exception.
		 *
		 * @return the element that is associated with this exception.
		 */

		public Element getElement()
		{
			return element;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: 'UNRECOGNISED XML ELEMENT' EXCEPTION


	/**
	 * This class implements an unchecked exception that is thrown in response to an unrecognised {@linkplain Element
	 * XML element}.
	 */

	public static class UnrecognisedElementException
		extends ElementException
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an exception that is associated with the specified XML element.
		 *
		 * @param element
		 *          the XML element that will be associated with this exception.
		 */

		public UnrecognisedElementException(
			Element	element)
		{
			// Call superclass constructor
			super(element);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: 'NO NAME ATTRIBUTE' EXCEPTION


	/**
	 * This class implements an unchecked exception that is thrown in response to an {@linkplain Element XML element}
	 * that is a member of an &lt;object&gt; element but has no <i>name</i> attribute.
	 */

	public static class NoNameAttributeException
		extends ElementException
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an exception that is associated with the specified XML element.
		 *
		 * @param element
		 *          the XML element that will be associated with this exception.
		 */

		public NoNameAttributeException(
			Element	element)
		{
			// Call superclass constructor
			super(element);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
