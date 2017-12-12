/*====================================================================*\

JsonBoolean.java

Class: JSON Boolean value.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// CLASS: JSON BOOLEAN VALUE


/**
 * This class implements an immutable JSON Boolean value.
 */

public class JsonBoolean
	extends JsonValue
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The literal name of a JSON Boolean whose value is <i>false</i>. */
	public static final	String	VALUE_FALSE	= "false";

	/** The literal name of a JSON Boolean whose value is <i>true</i>. */
	public static final	String	VALUE_TRUE	= "true";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON Boolean that has no parent and has the specified value.
	 *
	 * @param value
	 *          the value of the JSON Boolean.
	 */

	public JsonBoolean(boolean value)
	{
		// Call alternative constructor
		this(null, value);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON Boolean that has the specified parent and value.
	 *
	 * @param parent
	 *          the parent of the JSON Boolean.
	 * @param value
	 *          the value of the JSON Boolean.
	 */

	public JsonBoolean(JsonValue parent,
					   boolean   value)
	{
		// Call superclass constructor
		super(parent);

		// Initialise instance fields
		this.value = value;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @return {@link JsonValue.Kind#BOOLEAN}.
	 */

	@Override
	public Kind getKind()
	{
		return Kind.BOOLEAN;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * For a JSON Boolean, this method always returns {@code false}.
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
	 * Returns {@code true} if the specified object is an instance of {@code JsonBoolean} <i>and</i> it has the same
	 * value as this JSON Boolean.
	 *
	 * @param  obj
	 *           the object with which this JSON Boolean will be compared.
	 * @return {@code true} if <i>obj</i> is an instance of {@code JsonBoolean} <i>and</i> it has the same value as
	 *         this JSON Boolean; {@code false} otherwise.
	 */

	@Override
	public boolean equals(Object obj)
	{
		return (obj == this) || ((obj instanceof JsonBoolean) && (value == ((JsonBoolean)obj).value));
	}

	//------------------------------------------------------------------

	/**
	 * Returns the hash code of this JSON Boolean.
	 *
	 * @return the hash code of this JSON Boolean.
	 */

	@Override
	public int hashCode()
	{
		return Boolean.hashCode(value);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a copy of this JSON Boolean that has no parent, and returns the copy.
	 *
	 * @return a copy of this JSON Boolean that has no parent.
	 */

	@Override
	public JsonBoolean clone()
	{
		return (JsonBoolean)super.clone();
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of this JSON Boolean.
	 *
	 * @return a string representation of this JSON Boolean.
	 */

	@Override
	public String toString()
	{
		return value ? VALUE_TRUE : VALUE_FALSE;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the value of this JSON Boolean.
	 *
	 * @return the value of this JSON Boolean.
	 */

	public boolean getValue()
	{
		return value;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	/** The value of this JSON Boolean. */
	private	boolean	value;

}

//----------------------------------------------------------------------
