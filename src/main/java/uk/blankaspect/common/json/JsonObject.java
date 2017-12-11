/*====================================================================*\

JsonObject.java

Class: JSON object value.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//----------------------------------------------------------------------


// CLASS: JSON OBJECT VALUE


/**
 * This class implements a JSON object value.  A JSON object may contain zero or more <i>properties</i>.  A property is
 * a name&ndash;value pair whose name is a string and whose value is a {@linkplain JsonValue JSON value}.  The name of
 * a property is separated from the value with a ':' (U+003A).
 * <p>
 * An object begins with a '{' (U+007B) and ends with a '}' (U+007D).  Within an object, adjacent properties are
 * separated with a ',' (U+002C).
 * </p>
 */

public class JsonObject
	extends JsonValue
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The character that denotes the start of a JSON object. */
	public static final	char	START_CHAR				= '{';

	/** The character that denotes the end of a JSON object. */
	public static final	char	END_CHAR				= '}';

	/** The character that separates the name and value of a property of a JSON object. */
	public static final	char	NAME_SEPARATOR_CHAR		= ':';

	/** The character that separates adjacent properties of a JSON object. */
	public static final	char	PROPERTY_SEPARATOR_CHAR	= ',';

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: 'NO SUCH PROPERTY' EXCEPTION


	/**
	 * This class implements an unchecked exception that is thrown when an attempt is made to access a property of a
	 * {@linkplain JsonObject JSON object} by name and the JSON object does not contain a property with the specified
	 * name.
	 */

	public static class NoSuchPropertyException
		extends RuntimeException
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a <i>no such property</i> exception that is associated with the specified property
		 * name.
		 *
		 * @param name
		 *          the name of the property with which the exception is associated.
		 */

		public NoSuchPropertyException(String name)
		{
			// Initialise instance fields
			this.name = name;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the name of the property with which this exception is associated.
		 *
		 * @return the name of the property with which this exception is associated.
		 */

		public String getName()
		{
			return name;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		/** The name of the property with which this exception is associated. */
		private	String	name;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON object that has no parent and initially contains no properties.
	 */

	public JsonObject()
	{
		// Call alternative constructor
		this((JsonValue)null);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON object that has the specified parent and initially contains no properties.
	 *
	 * @param parent
	 *          the parent of the JSON object.
	 */

	public JsonObject(JsonValue parent)
	{
		// Call superclass constructor
		super(parent);

		// Initialise instance fields
		properties = new LinkedHashMap<>();
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON object that has no parent and initially contains the specified properties.
	 *
	 * @param properties
	 *          the initial properties of the JSON object.
	 */

	public JsonObject(Map<String, JsonValue> properties)
	{
		// Call alternative constructor
		this(null, properties);
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

	public JsonObject(JsonValue              parent,
					  Map<String, JsonValue> properties)
	{
		// Call alternative constructor
		this(parent);

		// Initialise instance fields
		addProperties(properties);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @return {@link JsonValue.Kind#OBJECT}.
	 */

	@Override
	public Kind getKind()
	{
		return Kind.OBJECT;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * For a JSON object, this method always returns {@code true}.
	 *
	 * @return {@code true}.
	 */

	@Override
	public boolean isContainer()
	{
		return true;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a list of the values of the properties of this JSON object.
	 *
	 * @return a list of the values of the properties of this JSON object.
	 */

	@Override
	public List<JsonValue> getChildren()
	{
		return new ArrayList<>(properties.values());
	}

	//------------------------------------------------------------------

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
		if (this == obj)
			return true;

		if (obj instanceof JsonObject)
		{
			JsonObject other = (JsonObject)obj;
			return (properties.size() == other.properties.size()) && properties.equals(other.properties);
		}
		return false;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the hash code of this JSON object, which is the hash code of its map of properties.
	 *
	 * @return the hash code of this JSON object.
	 */

	@Override
	public int hashCode()
	{
		return properties.hashCode();
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
	 * Returns {@code true} if this JSON object contains no properties.
	 *
	 * @return {@code true} if this JSON object contains no properties; {@code false} otherwise.
	 */

	public boolean isEmpty()
	{
		return (properties.size() == 0);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the number of properties that this JSON object contains.
	 *
	 * @return the number of properties that this JSON object contains.
	 */

	public int getNumProperties()
	{
		return properties.size();
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this JSON object contains a property with the specified name.
	 *
	 * @param  name
	 *           the name of the property of interest.
	 * @return {@code true} if this JSON object contains a property whose name is <i>name</i>; {@code false} otherwise.
	 */

	public boolean hasProperty(String name)
	{
		return properties.containsKey(name);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this JSON object contains a JSON-null property with the specified name.
	 *
	 * @param  name
	 *           the name of the property of interest.
	 * @return {@code true} if this JSON object contains a JSON-null property whose name is <i>name</i>; {@code false}
	 *         otherwise.
	 */

	public boolean hasNullProperty(String name)
	{
		return properties.get(name) instanceof JsonNull;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this JSON object contains a JSON-Boolean property with the specified name.
	 *
	 * @param  name
	 *           the name of the property of interest.
	 * @return {@code true} if this JSON object contains a JSON-Boolean property whose name is <i>name</i>; {@code
	 *         false} otherwise.
	 */

	public boolean hasBooleanProperty(String name)
	{
		return properties.get(name) instanceof JsonBoolean;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this JSON object contains a JSON-number property with the specified name.
	 *
	 * @param  name
	 *           the name of the property of interest.
	 * @return {@code true} if this JSON object contains a JSON-number property whose name is <i>name</i>; {@code false}
	 *         otherwise.
	 */

	public boolean hasNumberProperty(String name)
	{
		return properties.get(name) instanceof JsonNumber;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this JSON object contains a JSON-string property with the specified name.
	 *
	 * @param  name
	 *           the name of the property of interest.
	 * @return {@code true} if this JSON object contains a JSON-string property whose name is <i>name</i>; {@code false}
	 *         otherwise.
	 */

	public boolean hasStringProperty(String name)
	{
		return properties.get(name) instanceof JsonString;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this JSON object contains a JSON-object property with the specified name.
	 *
	 * @param  name
	 *           the name of the property of interest.
	 * @return {@code true} if this JSON object contains a JSON-object property whose name is <i>name</i>; {@code false}
	 *         otherwise.
	 */

	public boolean hasObjectProperty(String name)
	{
		return properties.get(name) instanceof JsonObject;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this JSON object contains a JSON-array property with the specified name.
	 *
	 * @param  name
	 *           the name of the property of interest.
	 * @return {@code true} if this JSON object contains a JSON-array property whose name is <i>name</i>; {@code false}
	 *         otherwise.
	 */

	public boolean hasArrayProperty(String name)
	{
		return properties.get(name) instanceof JsonArray;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a list of the names of the properties of this JSON object.  The names are in the order in which their
	 * associated properties were added to this JSON object.
	 *
	 * @return a list of the names of the properties of this JSON object.
	 */

	public List<String> getNames()
	{
		return new ArrayList<>(properties.keySet());
	}

	//------------------------------------------------------------------

	/**
	 * Returns the value of the property of this JSON object with the specified name.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, or {@code null} if this object
	 *         contains no such property.
	 */

	public JsonValue getValue(String name)
	{
		return properties.get(name);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonNull JSON null value}.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonNull}.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonNull}.
	 */

	public JsonNull getNull(String name)
	{
		return getNull(name, false);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonNull JSON null value}.  If
	 * there is no such property, this method either returns {@code null} or throws a {@link NoSuchPropertyException}
	 * according to the value of its <i>mustExist</i> argument.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @param  mustExist
	 *           if {@code true}, a {@code NoSuchPropertyException} will be thrown if this object does not contain a
	 *           property whose name is <i>name</i>; if {@code false}, {@code null} will be returned if this object does
	 *           not contain the named property.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonNull}, or
	 *         {@code null} if <i>mustExist</i> is {@code false} and this object contains no such property.
	 * @throws NoSuchPropertyException
	 *           if <i>mustExist</i> is {@code true} and this object does not contain a property whose name is
	 *           <i>name</i>.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonNull}.
	 */

	public JsonNull getNull(String  name,
							boolean mustExist)
	{
		if (mustExist && !properties.containsKey(name))
			throw new NoSuchPropertyException(name);

		return (JsonNull)properties.get(name);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonBoolean JSON Boolean}.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonBoolean}.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonBoolean}.
	 */

	public JsonBoolean getBoolean(String name)
	{
		return getBoolean(name, false);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonBoolean JSON Boolean}.  If
	 * there is no such property, this method either returns {@code null} or throws a {@link NoSuchPropertyException}
	 * according to the value of its <i>mustExist</i> argument.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @param  mustExist
	 *           if {@code true}, a {@code NoSuchPropertyException} will be thrown if this object does not contain a
	 *           property whose name is <i>name</i>; if {@code false}, {@code null} will be returned if this object does
	 *           not contain the named property.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonBoolean},
	 *         or {@code null} if <i>mustExist</i> is {@code false} and this object contains no such property.
	 * @throws NoSuchPropertyException
	 *           if <i>mustExist</i> is {@code true} and this object does not contain a property whose name is
	 *           <i>name</i>.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonBoolean}.
	 */

	public JsonBoolean getBoolean(String  name,
								  boolean mustExist)
	{
		if (mustExist && !properties.containsKey(name))
			throw new NoSuchPropertyException(name);

		return (JsonBoolean)properties.get(name);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonNumber JSON number}.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonNumber}.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonNumber}.
	 */

	public JsonNumber getNumber(String name)
	{
		return getNumber(name, false);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonNumber JSON number}.  If
	 * there is no such property, this method either returns {@code null} or throws a {@link NoSuchPropertyException}
	 * according to the value of its <i>mustExist</i> argument.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @param  mustExist
	 *           if {@code true}, a {@code NoSuchPropertyException} will be thrown if this object does not contain a
	 *           property whose name is <i>name</i>; if {@code false}, {@code null} will be returned if this object does
	 *           not contain the named property.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonNumber}, or
	 *         {@code null} if <i>mustExist</i> is {@code false} and this object contains no such property.
	 * @throws NoSuchPropertyException
	 *           if <i>mustExist</i> is {@code true} and this object does not contain a property whose name is
	 *           <i>name</i>.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonNumber}.
	 */

	public JsonNumber getNumber(String  name,
								boolean mustExist)
	{
		if (mustExist && !properties.containsKey(name))
			throw new NoSuchPropertyException(name);

		return (JsonNumber)properties.get(name);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonString JSON string}.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonString}.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonString}.
	 */

	public JsonString getString(String name)
	{
		return getString(name, false);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonString JSON string}.  If
	 * there is no such property, this method either returns {@code null} or throws a {@link NoSuchPropertyException}
	 * according to the value of its <i>mustExist</i> argument.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @param  mustExist
	 *           if {@code true}, a {@code NoSuchPropertyException} will be thrown if this object does not contain a
	 *           property whose name is <i>name</i>; if {@code false}, {@code null} will be returned if this object does
	 *           not contain the named property.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonString}, or
	 *         {@code null} if <i>mustExist</i> is {@code false} and this object contains no such property.
	 * @throws NoSuchPropertyException
	 *           if <i>mustExist</i> is {@code true} and this object does not contain a property whose name is
	 *           <i>name</i>.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonString}.
	 */

	public JsonString getString(String  name,
								boolean mustExist)
	{
		if (mustExist && !properties.containsKey(name))
			throw new NoSuchPropertyException(name);

		return (JsonString)properties.get(name);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonArray JSON array}.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonArray}.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonArray}.
	 */

	public JsonArray getArray(String name)
	{
		return getArray(name, false);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonArray JSON array}.  If
	 * there is no such property, this method either returns {@code null} or throws a {@link NoSuchPropertyException}
	 * according to the value of its <i>mustExist</i> argument.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @param  mustExist
	 *           if {@code true}, a {@code NoSuchPropertyException} will be thrown if this object does not contain a
	 *           property whose name is <i>name</i>; if {@code false}, {@code null} will be returned if this object does
	 *           not contain the named property.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonArray}, or
	 *         {@code null} if <i>mustExist</i> is {@code false} and this object contains no such property.
	 * @throws NoSuchPropertyException
	 *           if <i>mustExist</i> is {@code true} and this object does not contain a property whose name is
	 *           <i>name</i>.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonArray}.
	 */

	public JsonArray getArray(String  name,
							  boolean mustExist)
	{
		if (mustExist && !properties.containsKey(name))
			throw new NoSuchPropertyException(name);

		return (JsonArray)properties.get(name);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonObject JSON object}.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonObject}.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonObject}.
	 */

	public JsonObject getObject(String name)
	{
		return getObject(name, false);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the property of this JSON object with the specified name as a {@linkplain JsonObject JSON object}.  If
	 * there is no such property, this method either returns {@code null} or throws a {@link NoSuchPropertyException}
	 * according to the value of its <i>mustExist</i> argument.
	 *
	 * @param  name
	 *           the name of the property whose value is required.
	 * @param  mustExist
	 *           if {@code true}, a {@code NoSuchPropertyException} will be thrown if this object does not contain a
	 *           property whose name is <i>name</i>; if {@code false}, {@code null} will be returned if this object does
	 *           not contain the named property.
	 * @return the value of the property of this JSON object whose name is <i>name</i>, cast to a {@link JsonObject}, or
	 *         {@code null} if <i>mustExist</i> is {@code false} and this object contains no such property.
	 * @throws NoSuchPropertyException
	 *           if <i>mustExist</i> is {@code true} and this object does not contain a property whose name is
	 *           <i>name</i>.
	 * @throws ClassCastException
	 *           if the named property is not an instance of {@link JsonObject}.
	 */

	public JsonObject getObject(String  name,
								boolean mustExist)
	{
		if (mustExist && !properties.containsKey(name))
			throw new NoSuchPropertyException(name);

		return (JsonObject)properties.get(name);
	}

	//------------------------------------------------------------------

	/**
	 * Returns an unmodifiable map of the properties of this JSON object.  The iterator over the entries of the map
	 * ({@link Map#entrySet()}) returns the properties in the order in which they were added to this JSON object.
	 *
	 * @return an unmodifiable map of the properties of this JSON object.
	 */

	public Map<String, JsonValue> getProperties()
	{
		return Collections.unmodifiableMap(properties);
	}

	//------------------------------------------------------------------

	/**
	 * Sets the properties of this JSON object to the specified pairs of names and JSON values.
	 *
	 * @param properties
	 *          the pairs of names and JSON values to which the properties of this JSON object will be set.
	 */

	public void setProperties(Map<String, JsonValue> properties)
	{
		this.properties.clear();
		addProperties(properties);
	}

	//------------------------------------------------------------------

	/**
	 * Adds a property with the specified name and value to this JSON object.  If this object already contains a
	 * property with the specified name, the specified value will replace the value of the existing property without
	 * affecting the order of properties; otherwise, a new entry will be added to end of the map of properties.
	 *
	 * @param name
	 *          the name of the property.
	 * @param value
	 *          the value of the property.
	 */

	public void addProperty(String    name,
							JsonValue value)
	{
		properties.put(name, value);
		value.setParent(this);
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified pairs of names and JSON values to the properties of this JSON object.  If this object already
	 * contains a property with a specified name, the corresponding specified value will replace the value of the
	 * existing property without affecting the order of properties; otherwise, a new entry will be added to end of the
	 * map of properties.  Properties are added in the order in which they are returned by the iterator of the entries
	 * of the input map.
	 *
	 * @param properties
	 *          the pairs of names and JSON values that will be added to the end of the properties of this JSON object.
	 */

	public void addProperties(Map<String, JsonValue> properties)
	{
		for (Map.Entry<String, JsonValue> property : properties.entrySet())
			addProperty(property.getKey(), property.getValue());
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonNull JSON null value}, adds it to this JSON object as a property with
	 * the specified name and returns it.  If this object already contains a property with the specified name, the new
	 * JSON null value will replace the value of the existing property without affecting the order of properties;
	 * otherwise, a new entry will be added to end of the map of properties.
	 *
	 * @param  name
	 *           the name of the JSON-null property.
	 * @return the JSON null value that was created and added to this JSON object as a property whose name is
	 *         <i>name</i>.
	 */

	public JsonNull addNull(String name)
	{
		JsonNull nullValue = new JsonNull();
		addProperty(name, nullValue);
		return nullValue;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonBoolean JSON Boolean} with the specified value, adds it to this JSON
	 * object as a property with the specified name and returns it.  If this object already contains a property with the
	 * specified name, the new JSON Boolean will replace the value of the existing property without affecting the order
	 * of properties; otherwise, a new entry will be added to end of the map of properties.
	 *
	 * @param  name
	 *           the name of the JSON-Boolean property.
	 * @param  value
	 *           the value of the JSON-Boolean property.
	 * @return the JSON Boolean that was created from <i>value</i> and added to this JSON object as a property whose
	 *         name is <i>name</i>.
	 */

	public JsonBoolean addBoolean(String  name,
								  boolean value)
	{
		JsonBoolean jsonBoolean = new JsonBoolean(value);
		addProperty(name, jsonBoolean);
		return jsonBoolean;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonNumber JSON number} with the specified {@code int} value, adds it to
	 * this JSON object as a property with the specified name and returns it.  If this object already contains a
	 * property with the specified name, the new JSON number will replace the value of the existing property without
	 * affecting the order of properties; otherwise, a new entry will be added to end of the map of properties.
	 *
	 * @param  name
	 *           the name of the JSON-number property.
	 * @param  value  the value of the JSON-number property.
	 * @return the JSON number that was created from <i>value</i> and added to this JSON object as a property whose
	 *         name is <i>name</i>.
	 */

	public JsonNumber addNumber(String name,
								int    value)
	{
		JsonNumber jsonNumber = new JsonNumber(value);
		addProperty(name, jsonNumber);
		return jsonNumber;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonNumber JSON number} with the specified {@code long} value, adds it to
	 * this JSON object as a property with the specified name and returns it.  If this object already contains a
	 * property with the specified name, the new JSON number will replace the value of the existing property without
	 * affecting the order of properties; otherwise, a new entry will be added to end of the map of properties.
	 *
	 * @param  name
	 *           the name of the JSON-number property.
	 * @param  value
	 *           the value of the JSON-number property.
	 * @return the JSON number that was created from <i>value</i> and added to this JSON object as a property whose
	 *         name is <i>name</i>.
	 */

	public JsonNumber addNumber(String name,
								long   value)
	{
		JsonNumber jsonNumber = new JsonNumber(value);
		addProperty(name, jsonNumber);
		return jsonNumber;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonNumber JSON number} with the specified {@code double} value, adds it
	 * to this JSON object as a property with the specified name and returns it.  If this object already contains a
	 * property with the specified name, the new JSON number will replace the value of the existing property without
	 * affecting the order of properties; otherwise, a new entry will be added to end of the map of properties.
	 *
	 * @param  name
	 *           the name of the JSON-number property.
	 * @param  value
	 *           the value of the JSON-number property.
	 * @return the JSON number that was created from <i>value</i> and added to this JSON object as a property whose
	 *         name is <i>name</i>.
	 */

	public JsonNumber addNumber(String name,
								double value)
	{
		JsonNumber jsonNumber = new JsonNumber(value);
		addProperty(name, jsonNumber);
		return jsonNumber;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonString JSON string} with the specified value, adds it to this JSON
	 * object as a property with the specified name and returns it.  If this object already contains a property with the
	 * specified name, the new JSON string will replace the value of the existing property without affecting the order
	 * of properties; otherwise, a new entry will be added to end of the map of properties.
	 *
	 * @param  name
	 *           the name of the JSON-string property.
	 * @param  value
	 *           the value of the JSON-string property.
	 * @return the JSON string that was created from <i>value</i> and added to this JSON object as a property whose
	 *         name is <i>name</i>.
	 */

	public JsonString addString(String name,
								String value)
	{
		JsonString jsonString = new JsonString(value);
		addProperty(name, jsonString);
		return jsonString;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonArray JSON array} with the specified elements, adds it to this JSON
	 * object as a property with the specified name and returns it.  If this object already contains a property with the
	 * specified name, the new JSON array will replace the value of the existing property without affecting the order of
	 * properties; otherwise, a new entry will be added to end of the map of properties.
	 *
	 * @param  name
	 *           the name of the JSON-array property.
	 * @param  elements
	 *           the elements of the JSON array that will be created and added to this JSON object as a property whose
	 *           name is <i>name</i>.
	 * @return the JSON array that was created from <i>elements</i> and added to this JSON object as a property whose
	 *         name is <i>name</i>.
	 */

	public JsonArray addArray(String       name,
							  JsonValue... elements)
	{
		JsonArray jsonArray = new JsonArray(Arrays.asList(elements));
		addProperty(name, jsonArray);
		return jsonArray;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonArray JSON array} with the specified elements, adds it to this JSON
	 * object as a property with the specified name and returns it.  If this object already contains a property with the
	 * specified name, the new JSON array will replace the value of the existing property without affecting the order of
	 * properties; otherwise, a new entry will be added to end of the map of properties.
	 *
	 * @param  name
	 *           the name of the JSON-array property.
	 * @param  elements
	 *           the elements of the JSON array that will be created and added to this JSON object as a property whose
	 *           name is <i>name</i>.
	 * @return the JSON array that was created from <i>elements</i> and added to this JSON object as a property whose
	 *         name is <i>name</i>.
	 */

	public JsonArray addArray(String              name,
							  Iterable<JsonValue> elements)
	{
		JsonArray jsonArray = new JsonArray(elements);
		addProperty(name, jsonArray);
		return jsonArray;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonObject JSON object} with the specified properties, adds it to this
	 * JSON object as a property with the specified name and returns it.  If this object already contains a property
	 * with the specified name, the new JSON object will replace the value of the existing property without affecting
	 * the order of properties; otherwise, a new entry will be added to end of the map of properties.
	 *
	 * @param  name
	 *           the name of the JSON object property.
	 * @param  properties
	 *           the properties of the JSON object that will be created and added to this JSON object as a property
	 *           whose name is <i>name</i>.
	 * @return the JSON object that was created from <i>properties</i> and added to this JSON object as a property whose
	 *         name is <i>name</i>.
	 */

	public JsonObject addObject(String                 name,
								Map<String, JsonValue> properties)
	{
		JsonObject jsonObject = new JsonObject(properties);
		addProperty(name, jsonObject);
		return jsonObject;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	/** A map of the properties of this JSON object. */
	private	LinkedHashMap<String, JsonValue>	properties;

}

//----------------------------------------------------------------------
