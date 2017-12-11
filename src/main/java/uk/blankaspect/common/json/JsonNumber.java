/*====================================================================*\

JsonNumber.java

Class: JSON number value.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// CLASS: JSON NUMBER VALUE


/**
 * This class implements an immutable JSON number value.  The general representation of a JSON number is a decimal
 * floating-point number that consists of an optional leading minus sign, an integer part, an optional fraction part and
 * an optional exponent.  The various constructors of this class allow you to create an instance whose value is stored
 * as an {@link Integer}, a {@link Long} or a {@link Double}.  The {@link #getNumberType()} method returns a {@linkplain
 * NumberType constant} that denotes the underlying type of the value.
 */

public class JsonNumber
	extends JsonValue
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/**
	 * This is an enumeration of the types of Java number in which the value of a {@link JsonNumber} may be stored.
	 */

	public enum NumberType
	{
		/** The value is stored as an {@link Integer}. */
		INTEGER,

		/** The value is stored as a {@link Long}. */
		LONG,

		/** The value is stored as a {@link Double}. */
		DOUBLE
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON number that has no parent and has the specified value.  The value is stored as
	 * an {@link Integer}.
	 *
	 * @param value
	 *          the value of the JSON number.
	 */

	public JsonNumber(int value)
	{
		// Call alternative constructor
		this(null, value);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON number that has the specified parent and value.  The value is stored as an
	 * {@link Integer}.
	 *
	 * @param parent
	 *          the parent of the JSON number.
	 * @param value
	 *          the value of the JSON number.
	 */

	public JsonNumber(JsonValue parent,
					  int       value)
	{
		// Call superclass constructor
		super(parent);

		// Initialise instance fields
		numberType = NumberType.INTEGER;
		this.value = value;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON number that has no parent and has the specified value.  The value is stored as a
	 * {@link Long}.
	 *
	 * @param value
	 *          the value of the JSON number.
	 */

	public JsonNumber(long value)
	{
		// Call alternative constructor
		this(null, value);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON number value that has the specified parent and value.  The value is stored as a
	 * {@link Long}.
	 *
	 * @param parent
	 *          the parent of the JSON number.
	 * @param value
	 *          the value of the JSON number.
	 */

	public JsonNumber(JsonValue parent,
					  long      value)
	{
		// Call superclass constructor
		super(parent);

		// Initialise instance fields
		numberType = NumberType.LONG;
		this.value = value;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON number that has no parent and has the specified value.  The value is stored as a
	 * {@link Double}.
	 *
	 * @param value
	 *          the value of the JSON number.
	 */

	public JsonNumber(double value)
	{
		// Call alternative constructor
		this(null, value);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON number that has the specified parent and value.  The value is stored as a {@link
	 * Double}.
	 *
	 * @param parent
	 *          the parent of the JSON number.
	 * @param value
	 *          the value of the JSON number.
	 */

	public JsonNumber(JsonValue parent,
					  double    value)
	{
		// Call superclass constructor
		super(parent);

		// Initialise instance fields
		numberType = NumberType.DOUBLE;
		this.value = value;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @return {@link JsonValue.Kind#NUMBER}.
	 */

	@Override
	public Kind getKind()
	{
		return Kind.NUMBER;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * For a JSON number, this method always returns {@code false}.
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
	 * Returns {@code true} if the specified object is an instance of {@code JsonNumber} <i>and</i> it has the same
	 * value as this JSON number.
	 *
	 * @param  obj
	 *           the object with which this JSON number will be compared.
	 * @return {@code true} if <i>obj</i> is an instance of {@code JsonNumber} <i>and</i> it has the same value as this
	 *         JSON number; {@code false} otherwise.
	 */

	@Override
	public boolean equals(Object obj)
	{
		return (obj == this) || ((obj instanceof JsonNumber) && (value.equals(((JsonNumber)obj).value)));
	}

	//------------------------------------------------------------------

	/**
	 * Returns the hash code of this JSON number.
	 *
	 * @return the hash code of this JSON number.
	 */

	@Override
	public int hashCode()
	{
		return value.hashCode();
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of this JSON number.
	 *
	 * @return a string representation of this JSON number.
	 */

	@Override
	public String toString()
	{
		return value.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns a {@linkplain NumberType constant} that denotes the type of Java number in which the value of this JSON
	 * number is stored.
	 *
	 * @return a constant that denotes the type of Java number in which the value of this JSON number is stored.
	 */

	public NumberType getNumberType()
	{
		return numberType;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the value of this JSON number.  The type of the returned value corresponds to {@link #getNumberType()}.
	 *
	 * @return the value of this JSON number.
	 */

	public Number getValue()
	{
		return value;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the value of this JSON number as an {@code int}, which may involve type conversion that results in
	 * rounding or truncation.
	 *
	 * @return the value of this JSON number as an {@code int}.
	 */

	public int getInt()
	{
		return value.intValue();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the value of this JSON number as a {@code long}, which may involve type conversion that results in
	 * rounding or truncation.
	 *
	 * @return the value of this JSON number as a {@code long}.
	 */

	public long getLong()
	{
		return value.longValue();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the value of this JSON number as a {@code double}, which may involve type conversion that results in
	 * rounding.
	 *
	 * @return the value of this JSON number as a {@code double}.
	 */

	public double getDouble()
	{
		return value.doubleValue();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	/** An enumeration constant that denotes the type of {@link #value}. */
	private	NumberType	numberType;

	/** The value of this JSON number. */
	private	Number		value;

}

//----------------------------------------------------------------------
