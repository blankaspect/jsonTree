/*====================================================================*\

JsonNull.java

Class: JSON null value.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// CLASS: JSON NULL VALUE


/**
 * This class implements a JSON null value.
 */

public class JsonNull
	extends JsonValue
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The literal name of a JSON null value. */
	public static final	String	VALUE	= "null";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON null value that has no parent.
	 */

	public JsonNull()
	{
		// Call alternative constructor
		this(null);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON null value that has the specified parent.
	 *
	 * @param parent
	 *          the parent of the JSON null value.
	 */

	public JsonNull(JsonValue parent)
	{
		// Call superclass constructor
		super(parent);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @return {@link JsonValue.Kind#NULL}.
	 */

	@Override
	public Kind getKind()
	{
		return Kind.NULL;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * For a JSON null value, this method always returns {@code false}.
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
	 * Returns {@code true} if the specified object is an instance of {@code JsonNull}.
	 *
	 * @param  obj
	 *           the object with which this JSON null will be compared.
	 * @return {@code true} if <i>obj</i> is an instance of {@code JsonNull}; {@code false} otherwise.
	 */

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof JsonNull);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the hash code of this JSON null.
	 *
	 * @return the hash code of this JSON null.
	 */

	@Override
	public int hashCode()
	{
		return 1;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of this JSON null.
	 *
	 * @return a string representation of this JSON null.
	 */

	@Override
	public String toString()
	{
		return VALUE;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
