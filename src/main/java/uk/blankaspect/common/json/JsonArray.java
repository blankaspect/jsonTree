/*====================================================================*\

JsonArray.java

Class: JSON array value.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

//----------------------------------------------------------------------


// CLASS: JSON ARRAY VALUE


/**
 * This class implements a JSON array value.  A JSON array may contain zero or more <i>elements</i> that are {@linkplain
 * JsonValue JSON values}.  The elements may be of different types (eg, a mixture of JSON numbers and JSON strings).
 * <p>
 * An array begins with a '[' (U+005B) and ends with a ']' (U+005D).  Within an array, adjacent elements are separated
 * with a ',' (U+002C).
 * </p>
 */

public class JsonArray
	extends JsonValue
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The character that denotes the start of a JSON array. */
	public static final	char	START_CHAR	= '[';

	/** The character that denotes the end of a JSON array. */
	public static final	char	END_CHAR	= ']';

	/** The character that separates adjacent elements of a JSON array. */
	public static final	char	ELEMENT_SEPARATOR_CHAR	= ',';

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: 'ELEMENT KIND' EXCEPTION


	/**
	 * This class implements an unchecked exception that is thrown by a method that unwraps the elements of a
	 * {@linkplain JsonArray JSON array} if it finds that an element is not of the expected kind.
	 */

	public static class ElementKindException
		extends RuntimeException
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an <i>element kind</i> exception.
		 *
		 * @param message
		 *          the detail message of the exception.
		 */

		public ElementKindException(String message)
		{
			// Call superclass constructor
			super(message);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON array that has no parent and initially contains no elements.
	 */

	public JsonArray()
	{
		// Call alternative constructor
		this((JsonValue)null);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON array that has the specified parent and initially contains no elements.
	 *
	 * @param parent
	 *          the parent of the JSON array.
	 */

	public JsonArray(JsonValue parent)
	{
		// Call superclass constructor
		super(parent);

		// Initialise instance fields
		elements = new ArrayList<>();
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a JSON array that has no parent and initially contains the specified elements.
	 *
	 * @param elements
	 *          the initial elements of the JSON array.
	 */

	public JsonArray(Iterable<? extends JsonValue> elements)
	{
		// Call alternative constructor
		this(null, elements);
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

	public JsonArray(JsonValue    parent,
					 JsonValue... elements)
	{
		// Call alternative constructor
		this(parent, Arrays.asList(elements));
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

	public JsonArray(JsonValue                     parent,
					 Iterable<? extends JsonValue> elements)
	{
		// Call alternative constructor
		this(parent);

		// Initialise instance fields
		setElements(elements);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @return {@link JsonValue.Kind#ARRAY}.
	 */

	@Override
	public Kind getKind()
	{
		return Kind.ARRAY;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * For a JSON array, this method always returns {@code true}.
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
	 * Returns a list of the elements of this JSON array.
	 *
	 * @return a list of the elements of this JSON array.
	 */

	@Override
	public List<JsonValue> getChildren()
	{
		return new ArrayList<>(elements);
	}

	//------------------------------------------------------------------

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
		if (this == obj)
			return true;

		if (obj instanceof JsonArray)
		{
			JsonArray other = (JsonArray)obj;
			return (elements.size() == other.elements.size()) && elements.equals(other.elements);
		}
		return false;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the hash code of this JSON array, which is the hash code of the list of its elements.
	 *
	 * @return the hash code of this JSON array.
	 */

	@Override
	public int hashCode()
	{
		return elements.hashCode();
	}

	//------------------------------------------------------------------

	/**
	 * Creates a deep copy of this JSON array that has no parent, and returns the copy.
	 *
	 * @return a deep copy of this JSON array that has no parent.
	 */

	@Override
	public JsonArray clone()
	{
		// Create copy of this JSON array
		JsonArray copy = (JsonArray)super.clone();

		// Copy elements
		copy.elements = new ArrayList<>();
		for (JsonValue element : elements)
		{
			JsonValue value = element.clone();
			copy.elements.add(value);
			value.setParent(copy);
		}

		// Return copy
		return copy;
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

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if this JSON array contains no elements.
	 *
	 * @return {@code true} if this JSON array contains no elements; {@code false} otherwise.
	 */

	public boolean isEmpty()
	{
		return (elements.size() == 0);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the number of elements that this JSON array contains.
	 *
	 * @return the number of elements that this JSON array contains.
	 */

	public int getNumElements()
	{
		return elements.size();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the element of this JSON array at the specified index.
	 *
	 * @param  index
	 *           the index of the required element.
	 * @return the element of this JSON array at <i>index</i>.
	 * @throws IndexOutOfBoundsException
	 *           if (<i>index</i> &lt; 0) or (<i>index</i> &gt;= {@link #getNumElements()}).
	 */

	public JsonValue getElement(int index)
	{
		return elements.get(index);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the element of this JSON array at the specified index as a {@linkplain JsonNull JSON null value}.
	 *
	 * @param  index
	 *           the index of the required element.
	 * @return the element of this JSON array at <i>index</i>, cast to a {@link JsonNull}.
	 * @throws IndexOutOfBoundsException
	 *           if (<i>index</i> &lt; 0) or (<i>index</i> &gt;= {@link #getNumElements()}).
	 * @throws ClassCastException
	 *           if the element at <i>index</i> is not an instance of {@link JsonNull}.
	 */

	public JsonNull getNull(int index)
	{
		return (JsonNull)elements.get(index);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the element of this JSON array at the specified index as a {@linkplain JsonBoolean JSON Boolean}.
	 *
	 * @param  index
	 *           the index of the required element.
	 * @return the element of this JSON array at <i>index</i>, cast to a {@link JsonBoolean}.
	 * @throws IndexOutOfBoundsException
	 *           if (<i>index</i> &lt; 0) or (<i>index</i> &gt;= {@link #getNumElements()}).
	 * @throws ClassCastException
	 *           if the element at <i>index</i> is not an instance of {@link JsonBoolean}.
	 */

	public JsonBoolean getBoolean(int index)
	{
		return (JsonBoolean)elements.get(index);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the element of this JSON array at the specified index as a {@linkplain JsonNumber JSON number}.
	 *
	 * @param  index
	 *           the index of the required element.
	 * @return the element of this JSON array at <i>index</i>, cast to a {@link JsonNumber}.
	 * @throws IndexOutOfBoundsException
	 *           if (<i>index</i> &lt; 0) or (<i>index</i> &gt;= {@link #getNumElements()}).
	 * @throws ClassCastException
	 *           if the element at <i>index</i> is not an instance of {@link JsonNumber}.
	 */

	public JsonNumber getNumber(int index)
	{
		return (JsonNumber)elements.get(index);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the element of this JSON array at the specified index as a {@linkplain JsonString JSON number}.
	 *
	 * @param  index
	 *           the index of the required element.
	 * @return the element of this JSON array at <i>index</i>, cast to a {@link JsonString}.
	 * @throws IndexOutOfBoundsException
	 *           if (<i>index</i> &lt; 0) or (<i>index</i> &gt;= {@link #getNumElements()}).
	 * @throws ClassCastException
	 *           if the element at <i>index</i> is not an instance of {@link JsonString}.
	 */

	public JsonString getString(int index)
	{
		return (JsonString)elements.get(index);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the element of this JSON array at the specified index as a {@linkplain JsonArray JSON array}.
	 *
	 * @param  index
	 *           the index of the required element.
	 * @return the element of this JSON array at <i>index</i>, cast to a {@link JsonArray}.
	 * @throws IndexOutOfBoundsException
	 *           if (<i>index</i> &lt; 0) or (<i>index</i> &gt;= {@link #getNumElements()}).
	 * @throws ClassCastException
	 *           if the element at <i>index</i> is not an instance of {@link JsonArray}.
	 */

	public JsonArray getArray(int index)
	{
		return (JsonArray)elements.get(index);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the element of this JSON array at the specified index as a {@linkplain JsonObject JSON object}.
	 *
	 * @param  index
	 *           the index of the required element.
	 * @return the element of this JSON array at <i>index</i>, cast to a {@link JsonObject}.
	 * @throws IndexOutOfBoundsException
	 *           if (<i>index</i> &lt; 0) or (<i>index</i> &gt;= {@link #getNumElements()}).
	 * @throws ClassCastException
	 *           if the element at <i>index</i> is not an instance of {@link JsonObject}.
	 */

	public JsonObject getObject(int index)
	{
		return (JsonObject)elements.get(index);
	}

	//------------------------------------------------------------------

	/**
	 * Returns an unmodifiable list of the elements of this JSON array.
	 *
	 * @return an unmodifiable list of the elements of this JSON array.
	 */

	public List<JsonValue> getElements()
	{
		return Collections.unmodifiableList(elements);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying values of the elements of this JSON array as an array of {@code boolean}s.
	 *
	 * @return an array of the underlying {@code boolean} values of the elements of this JSON array.
	 * @throws ElementKindException
	 *           if any of the elements of this JSON array is not a {@linkplain JsonBoolean JSON Boolean}.
	 */

	public boolean[] getBooleanArray()
		throws ElementKindException
	{
		boolean[] outValues = new boolean[elements.size()];
		for (int i = 0; i < outValues.length; i++)
		{
			JsonValue element = elements.get(i);
			if (element.getKind() != Kind.BOOLEAN)
				throw new ElementKindException(element.getKind().toString());

			outValues[i] = ((JsonBoolean)element).getValue();
		}
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying values of the elements of this JSON array as a list of {@code Boolean}s.
	 *
	 * @return a list of the underlying {@code Boolean} values of the elements of this JSON array.
	 * @throws ElementKindException
	 *           if any of the elements of this JSON array is not a {@linkplain JsonBoolean JSON Boolean}.
	 */

	public List<Boolean> getBooleanList()
		throws ElementKindException
	{
		List<Boolean> outValues = new ArrayList<>();
		for (JsonValue element : elements)
		{
			if (element.getKind() != Kind.BOOLEAN)
				throw new ElementKindException(element.getKind().toString());

			outValues.add(((JsonBoolean)element).getValue());
		}
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying values of the elements of this JSON array as an array of {@code int}s.
	 *
	 * @return an array of the underlying {@code int} values of the elements of this JSON array.
	 * @throws ElementKindException
	 *           if any of the elements of this JSON array is not a {@linkplain JsonNumber JSON number} or the type of
	 *           the JSON number is not an {@linkplain JsonNumber.NumberType#INTEGER integer}.
	 */

	public int[] getIntArray()
		throws ElementKindException
	{
		int[] outValues = new int[elements.size()];
		for (int i = 0; i < outValues.length; i++)
		{
			JsonValue element = elements.get(i);
			if (element.getKind() != Kind.NUMBER)
				throw new ElementKindException(element.getKind().toString());

			JsonNumber number = (JsonNumber)element;
			if (number.getNumberType() != JsonNumber.NumberType.INTEGER)
				throw new ElementKindException(number.getNumberType().toString());

			outValues[i] = number.getInt();
		}
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying values of the elements of this JSON array as a list of {@code Integer}s.
	 *
	 * @return a list of the underlying {@code Integer} values of the elements of this JSON array.
	 * @throws ElementKindException
	 *           if any of the elements of this JSON array is not a {@linkplain JsonNumber JSON number} or the type of
	 *           the JSON number is not an {@linkplain JsonNumber.NumberType#INTEGER integer}.
	 */

	public List<Integer> getIntList()
		throws ElementKindException
	{
		List<Integer> outValues = new ArrayList<>();
		for (JsonValue element : elements)
		{
			if (element.getKind() != Kind.NUMBER)
				throw new ElementKindException(element.getKind().toString());

			JsonNumber number = (JsonNumber)element;
			if (number.getNumberType() != JsonNumber.NumberType.INTEGER)
				throw new ElementKindException(number.getNumberType().toString());

			outValues.add(number.getInt());
		}
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying values of the elements of this JSON array as an array of {@code long}s.
	 *
	 * @return an array of the underlying {@code long} values of the elements of this JSON array.
	 * @throws ElementKindException
	 *           if any of the elements of this JSON array is not a {@linkplain JsonNumber JSON number} or the type of
	 *           the JSON number is not a {@linkplain JsonNumber.NumberType#LONG long}.
	 */

	public long[] getLongArray()
		throws ElementKindException
	{
		long[] outValues = new long[elements.size()];
		for (int i = 0; i < outValues.length; i++)
		{
			JsonValue element = elements.get(i);
			if (element.getKind() != Kind.NUMBER)
				throw new ElementKindException(element.getKind().toString());

			JsonNumber number = (JsonNumber)element;
			if (number.getNumberType() != JsonNumber.NumberType.LONG)
				throw new ElementKindException(number.getNumberType().toString());

			outValues[i] = number.getLong();
		}
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying values of the elements of this JSON array as a list of {@code Long}s.
	 *
	 * @return a list of the underlying {@code Long} values of the elements of this JSON array.
	 * @throws ElementKindException
	 *           if any of the elements of this JSON array is not a {@linkplain JsonNumber JSON number} or the type of
	 *           the JSON number is not a {@linkplain JsonNumber.NumberType#LONG long}.
	 */

	public List<Long> getLongList()
		throws ElementKindException
	{
		List<Long> outValues = new ArrayList<>();
		for (JsonValue element : elements)
		{
			if (element.getKind() != Kind.NUMBER)
				throw new ElementKindException(element.getKind().toString());

			JsonNumber number = (JsonNumber)element;
			if (number.getNumberType() != JsonNumber.NumberType.LONG)
				throw new ElementKindException(number.getNumberType().toString());

			outValues.add(number.getLong());
		}
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying values of the elements of this JSON array as an array of {@code double}s.
	 *
	 * @return an array of the underlying {@code double} values of the elements of this JSON array.
	 * @throws ElementKindException
	 *           if any of the elements of this JSON array is not a {@linkplain JsonNumber JSON number} or the type of
	 *           the JSON number is not a {@linkplain JsonNumber.NumberType#DOUBLE double}.
	 */

	public double[] getDoubleArray()
		throws ElementKindException
	{
		double[] outValues = new double[elements.size()];
		for (int i = 0; i < outValues.length; i++)
		{
			JsonValue element = elements.get(i);
			if (element.getKind() != Kind.NUMBER)
				throw new ElementKindException(element.getKind().toString());

			JsonNumber number = (JsonNumber)element;
			if (number.getNumberType() != JsonNumber.NumberType.DOUBLE)
				throw new ElementKindException(number.getNumberType().toString());

			outValues[i] = number.getDouble();
		}
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying values of the elements of this JSON array as a list of {@code Double}s.
	 *
	 * @return a list of the underlying {@code Double} values of the elements of this JSON array.
	 * @throws ElementKindException
	 *           if any of the elements of this JSON array is not a {@linkplain JsonNumber JSON number} or the type of
	 *           the JSON number is not a {@linkplain JsonNumber.NumberType#DOUBLE double}.
	 */

	public List<Double> getDoubleList()
		throws ElementKindException
	{
		List<Double> outValues = new ArrayList<>();
		for (JsonValue element : elements)
		{
			if (element.getKind() != Kind.NUMBER)
				throw new ElementKindException(element.getKind().toString());

			JsonNumber number = (JsonNumber)element;
			if (number.getNumberType() != JsonNumber.NumberType.DOUBLE)
				throw new ElementKindException(number.getNumberType().toString());

			outValues.add(number.getDouble());
		}
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying values of the elements of this JSON array as an array of strings.
	 *
	 * @return an array of the underlying string values of the elements of this JSON array.
	 * @throws ElementKindException
	 *           if any of the elements of this JSON array is not a {@linkplain JsonString JSON string}.
	 */

	public String[] getStringArray()
		throws ElementKindException
	{
		String[] outValues = new String[elements.size()];
		for (int i = 0; i < outValues.length; i++)
		{
			JsonValue element = elements.get(i);
			if (element.getKind() != Kind.STRING)
				throw new ElementKindException(element.getKind().toString());

			outValues[i] = ((JsonString)element).getValue();
		}
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying values of the elements of this JSON array as a list of strings.
	 *
	 * @return a list of the underlying string values of the elements of this JSON array.
	 * @throws ElementKindException
	 *           if any of the elements of this JSON array is not a {@linkplain JsonString JSON string}.
	 */

	public List<String> getStringList()
		throws ElementKindException
	{
		List<String> outValues = new ArrayList<>();
		for (JsonValue element : elements)
		{
			if (element.getKind() != Kind.STRING)
				throw new ElementKindException(element.getKind().toString());

			outValues.add(((JsonString)element).getValue());
		}
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the index of the specified JSON value in the list of the elements of this array.  The value is compared
	 * for identity, not equality, with each element of the array until a match is found or all elements have been
	 * compared.
	 *
	 * @param  value
	 *           the value whose index is required.
	 * @return the index of <i>value</i> in the list of elements of this array, or -1 if <i>value</i> is not an element
	 *         of this array.
	 */

	public int indexOf(JsonValue value)
	{
		for (int i = 0; i < elements.size(); i++)
		{
			if (elements.get(i) == value)
				return i;
		}
		return -1;
	}

	//------------------------------------------------------------------

	/**
	 * Sets the elements of this JSON array to the specified JSON values.
	 *
	 * @param values
	 *          the values to which the elements of this JSON array will be set.
	 */

	public void setElements(JsonValue... values)
	{
		elements.clear();
		addElements(values);
	}

	//------------------------------------------------------------------

	/**
	 * Sets the elements of this JSON array to the specified JSON values.
	 *
	 * @param values
	 *          the values to which the elements of this JSON array will be set.
	 */

	public void setElements(Iterable<? extends JsonValue> values)
	{
		elements.clear();
		addElements(values);
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified JSON value to the end of the list of elements of this JSON array.
	 *
	 * @param value
	 *          the value that will be added to the end of the list of elements of this JSON array.
	 */

	public void addElement(JsonValue value)
	{
		elements.add(value);
		value.setParent(this);
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified JSON values to the end of the list of elements of this JSON array.  The values are added in
	 * the order of the arguments.
	 *
	 * @param values
	 *          the values that will be added to the end of the list of elements of this JSON array.
	 */

	public void addElements(JsonValue... values)
	{
		for (JsonValue value : values)
			addElement(value);
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified JSON values to the end of the list of elements of this JSON array.  The values are added in
	 * the order in which they are returned by their iterator.
	 *
	 * @param values
	 *          the values that will be added to the list of elements of this JSON array.
	 */

	public void addElements(Iterable<? extends JsonValue> values)
	{
		for (JsonValue value : values)
			addElement(value);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonNull JSON null value} and adds it to the end of the list of elements
	 * of this JSON array.
	 *
	 * @return the JSON null value that was created and added to the elements of this JSON array.
	 */

	public JsonNull addNull()
	{
		JsonNull nullValue = new JsonNull();
		addElement(nullValue);
		return nullValue;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonBoolean JSON Boolean} with the specified value and adds it to the end
	 * of the list of elements of this JSON array.
	 *
	 * @param  value
	 *           the value of the JSON Boolean that will be created and added to the elements of this JSON array.
	 * @return the JSON Boolean that was created from <i>value</i> and added to the elements of this JSON array.
	 */

	public JsonBoolean addBoolean(boolean value)
	{
		JsonBoolean jsonBoolean = new JsonBoolean(value);
		addElement(jsonBoolean);
		return jsonBoolean;
	}

	//------------------------------------------------------------------

	/**
	 * Creates new instances of {@linkplain JsonBoolean JSON Booleans} with the specified values and adds them to the
	 * end of the list of elements of this JSON array.  The values are added in the order of the arguments.
	 *
	 * @param values
	 *          the values for which JSON Booleans will be created and added to the elements of this JSON array.
	 */

	public void addBooleans(boolean... values)
	{
		for (boolean value : values)
			addElement(new JsonBoolean(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates new instances of {@linkplain JsonBoolean JSON Booleans} with the specified values and adds them to the
	 * end of the list of elements of this JSON array.  The values are added in the order of the arguments.
	 *
	 * @param values
	 *          the values for which JSON Booleans will be created and added to the elements of this JSON array.
	 */

	public void addBooleans(Iterable<Boolean> values)
	{
		for (Boolean value : values)
			addElement(new JsonBoolean(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonNumber JSON number} with the specified value and adds it to the end
	 * of the list of elements of this JSON array.
	 *
	 * @param  value
	 *           the value of the JSON number that will be created and added to the elements of this JSON array.
	 * @return the JSON number that was created from <i>value</i> and added to the elements of this JSON array.
	 */

	public JsonNumber addInt(int value)
	{
		JsonNumber jsonNumber = new JsonNumber(value);
		addElement(jsonNumber);
		return jsonNumber;
	}

	//------------------------------------------------------------------

	/**
	 * Creates new instances of {@linkplain JsonNumber JSON numbers} with the specified values and adds them to the end
	 * of the list of elements of this JSON array.  The values are added in the order of the arguments.
	 *
	 * @param values
	 *          the values for which JSON numbers will be created and added to the elements of this JSON array.
	 */

	public void addInts(int... values)
	{
		for (int value : values)
			addElement(new JsonNumber(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates new instances of {@linkplain JsonNumber JSON numbers} with the specified values and adds them to the end
	 * of the list of elements of this JSON array.  The values are added in the order of the arguments.
	 *
	 * @param values
	 *          the values for which JSON numbers will be created and added to the elements of this JSON array.
	 */

	public void addInts(Iterable<Integer> values)
	{
		for (Integer value : values)
			addElement(new JsonNumber(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonNumber JSON number} with the specified value and adds it to the end
	 * of the list of elements of this JSON array.
	 *
	 * @param  value
	 *           the value of the JSON number that will be created and added to the elements of this JSON array.
	 * @return the JSON number that was created from <i>value</i> and added to the elements of this JSON array.
	 */

	public JsonNumber addLong(long value)
	{
		JsonNumber jsonNumber = new JsonNumber(value);
		addElement(jsonNumber);
		return jsonNumber;
	}

	//------------------------------------------------------------------

	/**
	 * Creates new instances of {@linkplain JsonNumber JSON numbers} with the specified values and adds them to the end
	 * of the list of elements of this JSON array.  The values are added in the order of the arguments.
	 *
	 * @param values
	 *          the values for which JSON numbers will be created and added to the elements of this JSON array.
	 */

	public void addLongs(long... values)
	{
		for (long value : values)
			addElement(new JsonNumber(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates new instances of {@linkplain JsonNumber JSON numbers} with the specified values and adds them to the end
	 * of the list of elements of this JSON array.  The values are added in the order of the arguments.
	 *
	 * @param values
	 *          the values for which JSON numbers will be created and added to the elements of this JSON array.
	 */

	public void addLongs(Iterable<Long> values)
	{
		for (Long value : values)
			addElement(new JsonNumber(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonNumber JSON number} with the specified value and adds it to the end
	 * of the list of elements of this JSON array.
	 *
	 * @param  value
	 *           the value of the JSON number that will be created and added to the elements of this JSON array.
	 * @return the JSON number that was created from <i>value</i> and added to the elements of this JSON array.
	 */

	public JsonNumber addDouble(double value)
	{
		JsonNumber jsonNumber = new JsonNumber(value);
		addElement(jsonNumber);
		return jsonNumber;
	}

	//------------------------------------------------------------------

	/**
	 * Creates new instances of {@linkplain JsonNumber JSON numbers} with the specified values and adds them to the end
	 * of the list of elements of this JSON array.  The values are added in the order of the arguments.
	 *
	 * @param values
	 *          the values for which JSON numbers will be created and added to the elements of this JSON array.
	 */

	public void addDoubles(double... values)
	{
		for (double value : values)
			addElement(new JsonNumber(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates new instances of {@linkplain JsonNumber JSON numbers} with the specified values and adds them to the end
	 * of the list of elements of this JSON array.  The values are added in the order of the arguments.
	 *
	 * @param values
	 *          the values for which JSON numbers will be created and added to the elements of this JSON array.
	 */

	public void addDoubles(Iterable<Double> values)
	{
		for (Double value : values)
			addElement(new JsonNumber(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonString JSON string} with the specified value and adds it to the end
	 * of the list of elements of this JSON array.
	 *
	 * @param  value
	 *           the value of the JSON string that will be created and added to the elements of this JSON array.
	 * @return the JSON string that was created from <i>value</i> and added to the elements of this JSON array.
	 */

	public JsonString addString(String value)
	{
		JsonString jsonString = new JsonString(value);
		addElement(jsonString);
		return jsonString;
	}

	//------------------------------------------------------------------

	/**
	 * Creates new instances of {@linkplain JsonString JSON strings} with the specified values and adds them to the end
	 * of the list of elements of this JSON array.  The values are added in the order of the arguments.
	 *
	 * @param values
	 *          the values for which JSON strings will be created and added to the elements of this JSON array.
	 */

	public void addStrings(String... values)
	{
		for (String value : values)
			addElement(new JsonString(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates new instances of {@linkplain JsonString JSON strings} with the specified values and adds them to the end
	 * of the list of elements of this JSON array.  The values are added in the order of the arguments.
	 *
	 * @param values
	 *          the values for which JSON strings will be created and added to the elements of this JSON array.
	 */

	public void addStrings(Iterable<String> values)
	{
		for (String value : values)
			addElement(new JsonString(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonArray JSON array} with the specified elements, adds it to the end of
	 * the list of elements of this JSON array and returns it.
	 *
	 * @param  elements
	 *           the elements of the JSON array that will be created and added to the elements of this JSON array.
	 * @return the JSON array that was created from <i>elements</i> and added to the elements of this JSON array.
	 */

	public JsonArray addArray(JsonValue... elements)
	{
		JsonArray jsonArray = new JsonArray(Arrays.asList(elements));
		addElement(jsonArray);
		return jsonArray;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonArray JSON array} with the specified elements and adds it to the end
	 * of the list of elements of this JSON array.
	 *
	 * @param  elements
	 *           the elements of the JSON array that will be created and added to the elements of this JSON array.
	 * @return the JSON array that was created from <i>elements</i> and added to the elements of this JSON array.
	 */

	public JsonArray addArray(Iterable<JsonValue> elements)
	{
		JsonArray jsonArray = new JsonArray(elements);
		addElement(jsonArray);
		return jsonArray;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain JsonObject JSON object} with the specified properties and adds it to the
	 * end of the list of elements of this JSON array.
	 *
	 * @param  properties
	 *           the properties of the JSON object that will be created and added to the elements of this JSON array.
	 * @return the JSON object that was created from <i>properties</i> and added to the elements of this JSON array.
	 */

	public JsonObject addObject(Map<String, JsonValue> properties)
	{
		JsonObject jsonObject = new JsonObject(properties);
		addElement(jsonObject);
		return jsonObject;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	/** A list of the elements of this JSON array. */
	private	ArrayList<JsonValue>	elements;

}

//----------------------------------------------------------------------
