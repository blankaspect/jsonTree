/*====================================================================*\

JsonString.java

Class: JSON string value.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Arrays;

//----------------------------------------------------------------------


// CLASS: JSON STRING VALUE


/**
 * This class implements an immutable JSON string value.  A JSON string begins and ends with quotation marks, between
 * which are zero or more legal characters or escape sequences.  Within a JSON string, the following characters must be
 * escaped:
 * <ul>
 *   <li>a control character in the range U+0000 to U+001F inclusive,</li>
 *   <li>quotation mark, '"' (U+0022),</li>
 *   <li>reverse solidus, '\' (U+005C).</li>
 * </ul>
 */

public class JsonString
	extends JsonValue
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The character that denotes the start of a JSON string. */
	public static final		char	START_CHAR	= '"';

	/** The character that denotes the end of a JSON string. */
	public static final		char	END_CHAR	= START_CHAR;

	/** The character with which an escape sequence begins. */
	public static final		char	ESCAPE_PREFIX_CHAR	= '\\';

	/** The string with which an escape sequence begins. */
	public static final		String	ESCAPE_PREFIX		= Character.toString(ESCAPE_PREFIX_CHAR);

	/** The character that denotes a Unicode escape sequence, following the escape prefix. */
	public static final		char	UNICODE_ESCAPE_CHAR	= 'u';

	/** The number of the hexadecimal digits in a Unicode escape sequence. */
	public static final		int		UNICODE_LENGTH	= 4;

	/** A map from characters in an escape sequence to their corresponding literal characters. */
	private static final	char[][]	IN_ESCAPE_PAIRS	=
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

	/** A map from literal characters to their corresponding characters in an escape sequence. */
	private static final	char[][]	OUT_ESCAPE_PAIRS	=
	{
		{ '\\', '\\' },
		{ '\"', '\"' },
		{ '\b', 'b' },
		{ '\t', 't' },
		{ '\n', 'n' },
		{ '\f', 'f' },
		{ '\r', 'r' }
	};

	/** Miscellaneous strings. */
	private static final	String	INVALID_SEQUENCE_STR	= "Invalid escape sequence";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON string that has no parent and has the specified value.
	 *
	 * @param  value
	 *           the value of the JSON string.
	 * @throws IllegalArgumentException
	 *           if <i>value</i> is {@code null}.
	 */

	public JsonString(String value)
	{
		// Call alternative constructor
		this(null, value);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON string that has the specified parent and value.
	 *
	 * @param  parent
	 *           the parent of the JSON string.
	 * @param  value
	 *           the value of the JSON string.
	 * @throws IllegalArgumentException
	 *           if <i>value</i> is {@code null}.
	 */

	public JsonString(JsonValue parent,
					  String    value)
	{
		// Call superclass constructor
		super(parent);

		// Validate argument
		if (value == null)
			throw new IllegalArgumentException("Value is null");

		// Initialise instance fields
		this.value = value;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the representation of the Unicode value of the specified character as a string of four hexadecimal-digit
	 * characters.
	 *
	 * @param  ch
	 *           the character whose string representation of its Unicode value is required.
	 * @return the representation of the Unicode value of <i>ch</i> as a string four hexadecimal-digit characters.
	 */

	public static String charToCode(char ch)
	{
		String str = Integer.toHexString(ch).toUpperCase();
		int numZeros = UNICODE_LENGTH - str.length();
		if (numZeros > 0)
		{
			char[] zeros = new char[numZeros];
			Arrays.fill(zeros, '0');
			str = new String(zeros) + str;
		}
		return str;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the Unicode escape sequence of the specified character.
	 *
	 * @param  ch
	 *           the character whose Unicode escape sequence is required.
	 * @return the Unicode escape sequence of <i>ch</i>.
	 */

	public static String unicodeEscape(char ch)
	{
		return ESCAPE_PREFIX + UNICODE_ESCAPE_CHAR + charToCode(ch);
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string for the specified character sequence in which each of the following characters is replaced by
	 * its JSON escape sequence:
	 * <ul>
	 *   <li>quotation mark, '"' (U+0022),</li>
	 *   <li>reverse solidus, '\' (U+005C),</li>
	 *   <li>backspace (U+0008),</li>
	 *   <li>tab (U+0009),</li>
	 *   <li>line feed (U+000A),</li>
	 *   <li>form feed (U+000C),</li>
	 *   <li>carriage return (U+000D).</li>
	 * </ul>
	 * <p>
	 * In addition, each character in the input sequence that is not in the list above and lies outside the range U+0020
	 * to U+007E inclusive is replaced by its Unicode escape sequence, "&#x5C;u<i>nnnn</i>".
	 * </p>
	 * <p>
	 * Characters in the input sequence that are not escaped appear in the output string unchanged.
	 * </p>
	 * <p>
	 * The returned string will contain only printable characters from the US-ASCII character encoding (ie, characters
	 * in the range U+0020 to U+007E inclusive).
	 * </p>
	 *
	 * @param  seq
	 *           the character sequence that will be escaped.
	 * @return a string in which each character in <i>seq</i> that must be escaped or is greater than U+007E is
	 *         replaced by an escape sequence.
	 */

	public static String escape(CharSequence seq)
	{
		// Initialise buffer for output string
		int inLength = seq.length();
		StringBuilder buffer = new StringBuilder(inLength + inLength / 2);

		// Process input sequence
		for (int i = 0; i < inLength; i++)
		{
			// Get character from input sequence
			char ch = seq.charAt(i);

			// Search for standard escape sequence for character
			char[] pair = Arrays.stream(OUT_ESCAPE_PAIRS)
								.filter(pair0 -> (ch == pair0[0]))
								.findFirst()
								.orElse(null);

			// If there is a standard escape sequence for character, use it ...
			if (pair != null)
			{
				buffer.append(ESCAPE_PREFIX_CHAR);
				buffer.append(pair[1]);
			}

			// ... otherwise, if character is not 'printable', replace it with its Unicode escape sequence ...
			else if ((ch < '\u0020') || (ch > '\u007E'))
			{
				buffer.append(ESCAPE_PREFIX_CHAR);
				buffer.append(UNICODE_ESCAPE_CHAR);
				buffer.append(charToCode(ch));
			}

			// ... otherwise, append the character unchanged
			else
				buffer.append(ch);
		}

		// Return output string
		return buffer.toString();
	}

	//------------------------------------------------------------------

	/**
	 * Returns a JSON string for the specified character sequence.  The JSON string is a concatenation of a leading
	 * quotation mark, the string that is returned by calling {@link #escape(CharSequence)} on the input sequence, and a
	 * trailing quotation mark.
	 *
	 * @param  seq
	 *           the character sequence for which a JSON string is required.
	 * @return a JSON string for <i>seq</i>.
	 */

	public static String toJsonString(CharSequence seq)
	{
		return START_CHAR + escape(seq) + END_CHAR;
	}

	//------------------------------------------------------------------

	/**
	 * Parses the specified character sequence, which is assumed to be an escape sequence for a character without the
	 * '\' prefix, and returns the resulting literal character.
	 *
	 * @param  seq
	 *           the escape sequence that will be parsed.
	 * @return the literal character that results from parsing <i>seq</i> as an escape sequence for a character without
	 *         the '\' prefix.
	 */

	public static char parseEscape(CharSequence seq)
	{
		// Reject null or empty input sequence
		if ((seq == null) || (seq.length() == 0))
			throw new IllegalArgumentException(INVALID_SEQUENCE_STR);

		// Get first character of escape sequence
		char ch = seq.charAt(0);

		// If Unicode escape sequence, parse it as such ...
		if (ch == UNICODE_ESCAPE_CHAR)
		{
			if (seq.length() < UNICODE_LENGTH + 1)
				throw new IllegalArgumentException(INVALID_SEQUENCE_STR);
			ch = (char)Integer.parseUnsignedInt(seq.subSequence(1, UNICODE_LENGTH + 1).toString(), 16);
		}

		// ... otherwise, parse it as a non-Unicode escape sequence
		else
		{
			char ch0 = ch;
			char[] pair = Arrays.stream(IN_ESCAPE_PAIRS)
								.filter(pair0 -> (ch0 == pair0[0]))
								.findFirst()
								.orElse(null);
			if (pair == null)
				throw new IllegalArgumentException(INVALID_SEQUENCE_STR);
			ch = pair[1];
		}

		// Return literal character
		return ch;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @return {@link JsonValue.Kind#STRING}.
	 */

	@Override
	public Kind getKind()
	{
		return Kind.STRING;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * For a JSON string, this method always returns {@code false}.
	 *
	 * @return {@code false}.
	 */

	@Override
	public boolean isContainer()
	{
		return false;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the specified object is an instance of {@code JsonString} <i>and</i> it has the same
	 * value as this JSON string.
	 *
	 * @param  obj
	 *           the object with which this JSON string will be compared.
	 * @return {@code true} if <i>obj</i> is an instance of {@code JsonString} <i>and</i> it has the same value as this
	 *         JSON string; {@code false} otherwise.
	 */

	@Override
	public boolean equals(Object obj)
	{
		return (obj == this) || ((obj instanceof JsonString) && (value.equals(((JsonString)obj).value)));
	}

	//------------------------------------------------------------------

	/**
	 * Returns the hash code of this JSON string.
	 *
	 * @return the hash code of this JSON string.
	 */

	@Override
	public int hashCode()
	{
		return value.hashCode();
	}

	//------------------------------------------------------------------

	/**
	 * Creates a copy of this JSON string that has no parent, and returns the copy.
	 *
	 * @return a copy of this JSON string that has no parent.
	 */

	@Override
	public JsonString clone()
	{
		return (JsonString)super.clone();
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of this JSON string.
	 *
	 * @return a string representation of this JSON string.
	 */

	@Override
	public String toString()
	{
		return toJsonString(value);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the value of this JSON string.
	 *
	 * @return the value of this JSON string.
	 */

	public String getValue()
	{
		return value;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	/** The value of this JSON string. */
	private	String	value;

}

//----------------------------------------------------------------------
