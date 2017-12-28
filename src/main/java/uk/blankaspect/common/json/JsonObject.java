/*====================================================================*\

JsonObject.java

Class: JSON object value.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Map;

import uk.blankaspect.common.basictree.AbstractNode;
import uk.blankaspect.common.basictree.MapNode;

//----------------------------------------------------------------------


// CLASS: JSON OBJECT VALUE


/**
 * This class implements a JSON object.  A JSON object contains zero or more properties.  A property is a
 * name&ndash;value pair whose name is a string and whose value is a {@linkplain AbstractNode JSON value}.
 * <p>
 * An object begins with a '{' (U+007B) and ends with a '}' (U+007D).  The name and value of a property are separated
 * with a ':' (U+003A).  Adjacent properties are separated with a ',' (U+002C).
 * </p>
 */

public class JsonObject
	extends MapNode
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The character that separates the name and value of a property of a JSON object. */
	public static final	char	NAME_VALUE_SEPARATOR_CHAR	= KEY_VALUE_SEPARATOR_CHAR;

	/** The character that separates adjacent properties of a JSON object. */
	public static final	char	PROPERTY_SEPARATOR_CHAR		= PAIR_SEPARATOR_CHAR;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON object that has no parent and initially contains no properties.
	 */

	public JsonObject()
	{
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON object that has the specified parent and initially contains no properties.
	 *
	 * @param parent
	 *          the parent of the JSON object.
	 */

	public JsonObject(AbstractNode parent)
	{
		// Call superclass constructor
		super(parent);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON object that has no parent and initially contains the specified properties.
	 *
	 * @param properties
	 *          the initial properties of the JSON object.
	 */

	public JsonObject(Map<String, AbstractNode> properties)
	{
		// Call superclass constructor
		super(properties);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON object that has the specified parent and initially contains the specified
	 * properties.
	 *
	 * @param parent
	 *          the parent of the JSON object.
	 * @param properties
	 *          the initial properties of the JSON object.
	 */

	public JsonObject(AbstractNode              parent,
					  Map<String, AbstractNode> properties)
	{
		// Call superclass constructor
		super(parent, properties);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if the specified object is an instance of {@code JsonObject} <i>and</i> the two objects
	 * contain the same number of properties <i>and</i> the sets of properties of the two objects are equal to each
	 * other.
	 *
	 * @param  obj
	 *           the object with which this JSON object will be compared.
	 * @return {@code true} if <i>obj</i> is an instance of {@code JsonObject} <i>and</i> the two objects contain the
	 *         same number of properties <i>and</i> the sets of properties of the two objects are equal to each other;
	 *         {@code false} otherwise.
	 */

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof JsonObject) && super.equals(obj);
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a deep copy of this JSON object that has no parent.
	 *
	 * @return a deep copy of this JSON object that has no parent.
	 */

	@Override
	public JsonObject clone()
	{
		return (JsonObject)super.clone();
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of this JSON object.
	 *
	 * @return a string representation of this JSON object.
	 */

	@Override
	public String toString()
	{
		return new JsonGenerator().generate(this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if this JSON object contains a property whose name is the specified name and whose value is
	 * a {@linkplain JsonArray JSON array}.
	 *
	 * @param  name
	 *           the name of the property of interest.
	 * @return {@code true} if this JSON object contains a property whose name is <i>name</i> and whose value is a JSON
	 *         array; {@code false} otherwise.
	 */

	public boolean hasArray(String name)
	{
		return getValue(name) instanceof JsonArray;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this JSON object contains a property whose name is the specified name and whose value is
	 * a {@linkplain JsonObject JSON object}.
	 *
	 * @param  name
	 *           the name of the property of interest.
	 * @return {@code true} if this JSON object contains a property whose name is <i>name</i> and whose value is a JSON
	 *         object; {@code false} otherwise.
	 */

	public boolean hasObject(String name)
	{
		return getValue(name) instanceof JsonObject;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@linkplain JsonArray JSON array} that is associated with the specified name in this JSON object.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @return the JSON-array property that is associated with <i>name</i> in this JSON object, or {@code null} if this
	 *         JSON object does not contain a property with such a name.
	 * @throws ClassCastException
	 *           if this JSON object contains a property with the specified name and its value is not an instance of
	 *           {@link JsonArray}.
	 */

	public JsonArray getArray(String name)
	{
		return (JsonArray)getValue(name);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@linkplain JsonObject JSON object} that is associated with the specified name in this JSON object.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @return the JSON-object property that is associated with <i>name</i> in this JSON object, or {@code null} if this
	 *         JSON object does not contain a property with such a name.
	 * @throws ClassCastException
	 *           if this JSON object contains a property with the specified name and its value is not an instance of
	 *           {@link JsonObject}.
	 */

	public JsonObject getObject(String name)
	{
		return (JsonObject)getValue(name);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
