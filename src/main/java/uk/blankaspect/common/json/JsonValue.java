/*====================================================================*\

JsonValue.java

Class: JSON value.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import uk.blankaspect.common.tree.ITreeNode;
import uk.blankaspect.common.tree.TreeUtils;

//----------------------------------------------------------------------


// CLASS: JSON VALUE


/**
 * This is the abstract base class of a JSON value.  It implements {@link ITreeNode} to allow the methods of {@link
 * TreeUtils} to be used on a tree of JSON values.
 */

public abstract class JsonValue
	implements ITreeNode<JsonValue>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/**
	 * This is an enumeration of the kinds of JSON value.
	 */

	public enum Kind
	{
		/**
		 * A null value.
		 */
		NULL,

		/**
		 * A Boolean value.
		 */
		BOOLEAN,

		/**
		 * A number value.
		 */
		NUMBER,

		/**
		 * A string value.
		 */
		STRING,

		/**
		 * An object value.
		 */
		OBJECT,

		/**
		 * An array value.
		 */
		ARRAY
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a JSON value with the specified parent.
	 *
	 * @param parent
	 *          the parent of the JSON value.
	 */

	protected JsonValue(JsonValue parent)
	{
		// Initialise instance fields
		this.parent = parent;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the kind of this JSON value.
	 *
	 * @return the kind of this JSON value.
	 */

	public abstract Kind getKind();

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this JSON value is a container.
	 *
	 * @return {@code true} if this JSON value is a container, {@code false} otherwise.
	 */

	public abstract boolean isContainer();

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ITreeNode interface
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the parent of this JSON value.
	 *
	 * @return the parent of this JSON value, or {@code null} if this JSON value has no parent.
	 */

	@Override
	public JsonValue getParent()
	{
		return parent;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a list of the children of this JSON value.
	 *
	 * @return a list of the children of this JSON value.
	 */

	@Override
	public List<JsonValue> getChildren()
	{
		return Collections.emptyList();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if this JSON value is the root of the tree to which it belongs.  A JSON value is deemed to
	 * be the root if it has no parent.
	 *
	 * @return {@code true} if this JSON value is the root of the tree to which it belongs.
	 */

	public boolean isRoot()
	{
		return (parent == null);
	}

	//------------------------------------------------------------------

	/**
	 * Sets the parent of this JSON value to the specified JSON value.
	 *
	 * @param parent
	 *          the JSON value to which the parent of this JSON value will be set, which should be {@code null} if this
	 *          JSON value has no parent.
	 */

	public void setParent(JsonValue parent)
	{
		this.parent = parent;
	}

	//------------------------------------------------------------------

	/**
	 * If this JSON value is the value of a property of a {@linkplain JsonObject JSON object}, returns the name of the
	 * property.
	 *
	 * @return if this JSON value is the value of a property of a JSON object, the name of the property; otherwise,
	 *         {@code null}.
	 */

	public String getPropertyName()
	{
		if (parent instanceof JsonObject)
		{
			JsonObject jsonObj = (JsonObject)parent;
			for (String name : jsonObj.getNames())
			{
				if (jsonObj.getValue(name) == this)
					return name;
			}
		}
		return null;
	}

	//------------------------------------------------------------------

	/**
	 * If this JSON value is an element of a {@linkplain JsonArray JSON array}, returns the index of the element in the
	 * array's list of elements.
	 *
	 * @return if this JSON value is an element of a JSON array, the index of the element; otherwise, -1.
	 */

	public int getElementIndex()
	{
		return (parent instanceof JsonArray) ? ((JsonArray)parent).indexOf(this) : -1;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the path of this JSON value from the root of the tree to which it belongs.  The path is a list of
	 * <i>n</i> JSON values, where <i>n</i> &gt; 0.  The first <i>n</i>-1 elements of the list are the ancestors of
	 * this value, starting with the root and ending with the parent of this value; the last element is this value.
	 *
	 * @return a list that is the concatenation of
	 *         <ul>
	 *           <li>a sequence of the ancestors of this JSON value, starting with the root of the tree to which it
	 *               belongs and ending with its parent, and</li>
	 *           <li>this value.</li>
	 *         </ul>
	 */

	public List<JsonValue> getPath()
	{
		LinkedList<JsonValue> path = new LinkedList<>();
		JsonValue value = this;
		while (value != null)
		{
			path.addFirst(value);
			value = value.parent;
		}
		return path;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	/** The parent of this JSON value. */
	private	JsonValue	parent;

}

//----------------------------------------------------------------------
