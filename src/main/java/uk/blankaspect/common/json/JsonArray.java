/*====================================================================*\

JsonArray.java

Class: JSON array value.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// IMPORTS


import uk.blankaspect.common.basictree.AbstractNode;
import uk.blankaspect.common.basictree.ListNode;

//----------------------------------------------------------------------


// CLASS: JSON ARRAY VALUE


/**
 * This class implements a JSON array.  A JSON array contains zero or more elements that are {@linkplain AbstractNode
 * JSON values}.  The elements may be of different types (eg, a mixture of JSON numbers and JSON strings).
 * <p>
 * An array begins with a '[' (U+005B) and ends with a ']' (U+005D).  Adjacent elements are separated with a ','
 * (U+002C).
 * </p>
 */

public class JsonArray
	extends ListNode
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON array that has no parent and initially contains no elements.
	 */

	public JsonArray()
	{
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON array that has the specified parent and initially contains no elements.
	 *
	 * @param parent
	 *          the parent of the JSON array.
	 */

	public JsonArray(AbstractNode parent)
	{
		// Call superclass constructor
		super(parent);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON array that has no parent and initially contains the specified elements.
	 *
	 * @param elements
	 *          the initial elements of the JSON array.
	 */

	public JsonArray(Iterable<? extends AbstractNode> elements)
	{
		// Call superclass constructor
		super(null, elements);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON array that has the specified parent and initially contains the specified
	 * elements.
	 *
	 * @param parent
	 *          the parent of the JSON array.
	 * @param elements
	 *          the initial elements of the JSON array.
	 */

	public JsonArray(AbstractNode    parent,
					 AbstractNode... elements)
	{
		// Call superclass constructor
		super(parent, elements);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON array that has the specified parent and initially contains the specified
	 * elements.
	 *
	 * @param parent
	 *          the parent of the JSON array.
	 * @param elements
	 *          the initial elements of the JSON array.
	 */

	public JsonArray(AbstractNode                     parent,
					 Iterable<? extends AbstractNode> elements)
	{
		// Call superclass constructor
		super(parent, elements);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if the specified object is an instance of {@code JsonArray} <i>and</i> the two arrays
	 * contain the same number of elements <i>and</i> the elements at the same index in the two arrays are equal to each
	 * other.
	 *
	 * @param  obj
	 *           the object with which this JSON array will be compared.
	 * @return {@code true} if <i>obj</i> is an instance of {@code JsonArray} <i>and</i> the two arrays contain the
	 *         same number of elements <i>and</i> the elements at the same index in the two arrays are equal to each
	 *         other; {@code false} otherwise.
	 */

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof JsonArray) && super.equals(obj);
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a deep copy of this JSON array that has no parent.
	 *
	 * @return a deep copy of this JSON array that has no parent.
	 */

	@Override
	public JsonArray clone()
	{
		return (JsonArray)super.clone();
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of this JSON array.
	 *
	 * @return a string representation of this JSON array.
	 */

	@Override
	public String toString()
	{
		return new JsonGenerator().generate(this);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
