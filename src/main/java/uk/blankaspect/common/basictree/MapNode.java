/*====================================================================*\

MapNode.java

Class: map node.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.basictree;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.function.Function;

//----------------------------------------------------------------------


// CLASS: MAP NODE


/**
 * This class implements a {@linkplain AbstractNode node} that contains a collection of key&ndash;value pairs whose keys
 * are strings and whose values are {@linkplain AbstractNode node}s.  In the documentation of this class,
 * <i>key&ndash;value pair</i> is sometimes abbreviated to <i>KV pair</i> or just <i>pair</i>.
 * <p>
 * A map node preserves the order of the key&ndash;value pairs that are added to it; that is, an iterator over the
 * collection of KV pairs will traverse the pairs in the order in which their <i>keys</i> were added to the map node.
 * (The same is true of iterators over the collections of keys ({@link #getKeys()}) or values ({@link #getChildren()}).)
 * If a KV pair is added to a map node that already contains a KV pair with the key of the new pair, the value of the
 * new pair will replace the old value without affecting the order of the KV pairs.
 * </p>
 * <p>
 * A map node may be created with an initial collection of key&ndash;value pairs, and KV pairs may be added to a map
 * node after its creation, but KV pairs cannot be removed from a map node.
 * </p>
 * <p>
 * The default string representation of a map node begins with a '{' (U+007B) and ends with a '}' (U+007D).  The key of
 * a KV pair is escaped and enclosed in quotation marks in the same way as the value of a {@linkplain StringNode string
 * node}.  The key and value of a KV pair are separated with a ':' (U+003A).  Adjacent KV pairs are separated with a ','
 * (U+002C).
 * </p>
 */

public class MapNode
	extends AbstractNode
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The character that denotes the start of the string representation of a map node. */
	public static final	char	START_CHAR	= '{';

	/** The character that denotes the end of the string representation of a map node. */
	public static final	char	END_CHAR	= '}';

	/** The character that separates the key and value of a KV pair in the string representation of a map node. */
	public static final	char	KEY_VALUE_SEPARATOR_CHAR	= ':';

	/** The character that separates adjacent KV pairs in the string representation of a map node. */
	public static final	char	PAIR_SEPARATOR_CHAR			= ',';

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: KEY-VALUE PAIR


	/**
	 * This class encapsulates a key&ndash;value pair of a {@linkplain MapNode map node}.  The key is a string and the
	 * value is a {@linkplain AbstractNode node}.
	 */

	public static class Pair
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a key&ndash;value pair of a {@linkplain MapNode map node}.
		 *
		 * @param key
		 *          the key of the key&ndash;value pair.
		 * @param value
		 *          the value of the key&ndash;value pair.
		 */

		public Pair(String       key,
					AbstractNode value)
		{
			// Validate arguments
			if (key == null)
				throw new IllegalArgumentException("Null key");
			if (value == null)
				throw new IllegalArgumentException("Null value");

			// Initialise instance fields
			this.key = key;
			this.value = value;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Sets the function that converts the key of a key&ndash;value pair to its string representation for the {@link
		 * #toString()} method.  The default converter is the method {@link StringNode#escapeAndQuote(CharSequence)}.
		 *
		 * @param converter  the function that converts the key of a key&ndash;value pair to a string representation.
		 */

		public static void setKeyConverter(Function<CharSequence, String> converter)
		{
			keyConverter = converter;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns {@code true} if the specified object is an instance of {@code Pair} <i>and</i> the keys and values of
		 * the two pairs are equal to each other.
		 *
		 * @param  obj
		 *           the object with which this KV pair will be compared.
		 * @return {@code true} if <i>obj</i> is an instance of {@code Pair} <i>and</i> the keys and values of the two
		 *         pairs are equal to each other; {@code false} otherwise.
		 */

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;

			if (obj instanceof Pair)
			{
				Pair other = (Pair)obj;
				return key.equals(other.key) && value.equals(other.value);
			}
			return false;
		}

		//--------------------------------------------------------------

		/**
		 * Returns the hash code of this key&ndash;value pair.
		 *
		 * @return the hash code of this key&ndash;value pair.
		 */

		@Override
		public int hashCode()
		{
			return key.hashCode() * 31 + value.hashCode();
		}

		//--------------------------------------------------------------

		/**
		 * Creates and returns a copy of this key&ndash;value pair.
		 *
		 * @return a copy of this key&ndash;value pair.
		 */

		@Override
		public Pair clone()
		{
			try
			{
				// Create copy of this pair
				Pair copy = (Pair)super.clone();

				// Create copy of value
				copy.value = value.clone();

				// Return copy
				return copy;
			}
			catch (CloneNotSupportedException e)
			{
				throw new RuntimeException("Unexpected exception", e);
			}
		}

		//--------------------------------------------------------------

		/**
		 * Returns a string representation of this pair.
		 *
		 * @return a string representation of this pair.
		 */

		@Override
		public String toString()
		{
			return keyConverter.apply(key) + KEY_VALUE_SEPARATOR_CHAR + ' ' + value;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the key of this key&ndash;value pair.
		 *
		 * @return the key of this key&ndash;value pair.
		 */

		public String getKey()
		{
			return key;
		}

		//--------------------------------------------------------------

		/**
		 * Returns the value of this key&ndash;value pair.
		 *
		 * @return the value of this key&ndash;value pair.
		 */

		public AbstractNode getValue()
		{
			return value;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class fields
	////////////////////////////////////////////////////////////////////

		/** The function that converts the key of a key&ndash;value pair to its string representation for {@link
			#toString()}. */
		private static	Function<CharSequence, String>	keyConverter	= StringNode::escapeAndQuote;

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		/** The key of this key&ndash;value pair. */
		private	String			key;

		/** The value of this key&ndash;value pair. */
		private	AbstractNode	value;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a map node that has no parent and initially contains no key&ndash;value pairs.
	 */

	public MapNode()
	{
		// Call alternative constructor
		this((AbstractNode)null);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a map node that has the specified parent and initially contains no key&ndash;value
	 * pairs.
	 *
	 * @param parent
	 *          the parent of the map node.
	 */

	public MapNode(AbstractNode parent)
	{
		// Call superclass constructor
		super(parent);

		// Initialise instance fields
		pairs = new LinkedHashMap<>();
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a map node that has no parent and initially contains the specified key&ndash;value
	 * pairs.
	 *
	 * @param pairs
	 *          the initial key&ndash;value pairs of the map node.
	 */

	public MapNode(Pair... pairs)
	{
		// Call alternative constructor
		this(null, Arrays.asList(pairs));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a map node that has no parent and initially contains the specified key&ndash;value
	 * pairs.
	 *
	 * @param pairs
	 *          the initial key&ndash;value pairs of the map node.
	 */

	public MapNode(Iterable<? extends Pair> pairs)
	{
		// Call alternative constructor
		this(null, pairs);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a map node that has no parent and initially contains the specified key&ndash;value
	 * pairs.
	 *
	 * @param pairs
	 *          the initial key&ndash;value pairs of the map node.
	 */

	public MapNode(Map<String, AbstractNode> pairs)
	{
		// Call alternative constructor
		this(null, pairs);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a map node that has the specified parent and initially contains the specified
	 * key&ndash;value pairs.
	 *
	 * @param parent
	 *          the parent of the map node.
	 * @param pairs
	 *          the initial key&ndash;value pairs of the map node.
	 */

	public MapNode(AbstractNode parent,
				   Pair...      pairs)
	{
		// Call alternative constructor
		this(parent, Arrays.asList(pairs));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a map node that has the specified parent and initially contains the specified
	 * key&ndash;value pairs.
	 *
	 * @param parent
	 *          the parent of the map node.
	 * @param pairs
	 *          the initial key&ndash;value pairs of the map node.
	 */

	public MapNode(AbstractNode             parent,
				   Iterable<? extends Pair> pairs)
	{
		// Call alternative constructor
		this(parent);

		// Initialise instance fields
		addPairs(pairs);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a map node that has the specified parent and initially contains the specified
	 * key&ndash;value pairs.
	 *
	 * @param parent
	 *          the parent of the map node.
	 * @param pairs
	 *          the initial key&ndash;value pairs of the map node.
	 */

	public MapNode(AbstractNode              parent,
				   Map<String, AbstractNode> pairs)
	{
		// Call alternative constructor
		this(parent);

		// Initialise instance fields
		addPairs(pairs);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a {@linkplain BooleanNode Boolean node} for the specified value, creates a {@linkplain Pair
	 * key&ndash;value pair} whose key is the specified key and whose value is the Boolean node, and returns the KV
	 * pair.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair.
	 * @param  value
	 *           the value of the Boolean node.
	 * @return a key&ndash;value pair whose key is <i>key</i> and whose value is a new Boolean node whose value is
	 *         <i>value</i>.
	 */

	public static Pair pair(String  key,
							boolean value)
	{
		return new Pair(key, new BooleanNode(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates an {@linkplain IntNode 'int' node} for the specified value, creates a {@linkplain Pair key&ndash;value
	 * pair} whose key is the specified key and whose value is the 'int' node, and returns the KV pair.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair.
	 * @param  value
	 *           the value of the 'int' node.
	 * @return a key&ndash;value pair whose key is <i>key</i> and whose value is a new 'int' node whose value is
	 *         <i>value</i>.
	 */

	public static Pair pair(String key,
							int    value)
	{
		return new Pair(key, new IntNode(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a {@linkplain LongNode 'long' node} for the specified value, creates a {@linkplain Pair key&ndash;value
	 * pair} whose key is the specified key and whose value is the 'long' node, and returns the KV pair.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair.
	 * @param  value
	 *           the value of the 'long' node.
	 * @return a key&ndash;value pair whose key is <i>key</i> and whose value is a new 'long' node whose value is
	 *         <i>value</i>.
	 */

	public static Pair pair(String key,
							long   value)
	{
		return new Pair(key, new LongNode(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a {@linkplain DoubleNode 'double' node} for the specified value, creates a {@linkplain Pair
	 * key&ndash;value pair} whose key is the specified key and whose value is the 'double' node, and returns the KV
	 * pair.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair.
	 * @param  value
	 *           the value of the 'double' node.
	 * @return a key&ndash;value pair whose key is <i>key</i> and whose value is a new 'double' node whose value is
	 *         <i>value</i>.
	 */

	public static Pair pair(String key,
							double value)
	{
		return new Pair(key, new DoubleNode(value));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a {@linkplain StringNode string node} for the specified value, creates a {@linkplain Pair key&ndash;value
	 * pair} whose key is the specified key and whose value is the string node, and returns the KV pair.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair.
	 * @param  value
	 *           the value of the string node.
	 * @return a key&ndash;value pair whose key is <i>key</i> and whose value is a new string node whose value is
	 *         <i>value</i>.
	 */

	public static Pair pair(String key,
							String value)
	{
		return new Pair(key, new StringNode(value));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @return {@link NodeKind#MAP}.
	 */

	@Override
	public NodeKind getKind()
	{
		return NodeKind.MAP;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * For a map node, this method always returns {@code true}.
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
	 * Returns a list of the values of the key&ndash;value pairs of this map node.  The list may be modified without
	 * affecting this map node, but modifying the elements of the list (for example, changing the parent of a node)
	 * <i>will</i> affect this map node.
	 *
	 * @return a list of the values of the key&ndash;value pairs of this map node.
	 * @see    #getKeys()
	 * @see    #getPairList()
	 * @see    #getPairMap()
	 */

	@Override
	public List<AbstractNode> getChildren()
	{
		return new ArrayList<>(pairs.values());
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the specified object is an instance of {@code MapNode} <i>and</i> this map node contains
	 * the same number of key&ndash;value pairs as the other map node <i>and</i> for each KV pair in this map node,
	 * <i>kv1</i>, there is a KV pair in the other map node, <i>kv2</i>, for which the keys of <i>kv1</i> and
	 * <i>kv2</i> are equal and the values of <i>kv1</i> and <i>kv2</i> are equal.
	 *
	 * @param  obj
	 *           the object with which this map node will be compared.
	 * @return {@code true} if <i>obj</i> is an instance of {@code MapNode} <i>and</i> this map node contains the same
	 *         number of key&ndash;value pairs as the other map node <i>and</i> for each KV pair in this map node,
	 *         <i>kv1</i>, there is a KV pair in the other map node, <i>kv2</i>, for which the keys of <i>kv1</i> and
	 *         <i>kv2</i> are equal and the values of <i>kv1</i> and <i>kv2</i> are equal; {@code false} otherwise.
	 */

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (obj instanceof MapNode)
		{
			MapNode other = (MapNode)obj;
			return (pairs.size() == other.pairs.size()) && pairs.equals(other.pairs);
		}
		return false;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the hash code of this map node, which is the hash code of its key&ndash;value pairs.
	 *
	 * @return the hash code of this map node.
	 */

	@Override
	public int hashCode()
	{
		return pairs.hashCode();
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a deep copy of this map node that has no parent.
	 *
	 * @return a deep copy of this map node that has no parent.
	 */

	@Override
	public MapNode clone()
	{
		// Create copy of this map node
		MapNode copy = (MapNode)super.clone();

		// Copy KV pairs
		copy.pairs = new LinkedHashMap<>();
		for (Map.Entry<String, AbstractNode> pair : pairs.entrySet())
		{
			AbstractNode value = pair.getValue().clone();
			copy.pairs.put(pair.getKey(), value);
			value.setParent(copy);
		}

		// Return copy
		return copy;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of this map node.
	 *
	 * @return a string representation of this map node.
	 */

	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder(128);
		Iterator<Map.Entry<String, AbstractNode>> it = pairs.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, AbstractNode> pair = it.next();
			buffer.append(keyToString(pair.getKey()));
			buffer.append(KEY_VALUE_SEPARATOR_CHAR);
			buffer.append(' ');
			buffer.append(pair.getValue());
			if (it.hasNext())
			{
				buffer.append(PAIR_SEPARATOR_CHAR);
				buffer.append(' ');
			}
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if this map node contains no key&ndash;value pairs.
	 *
	 * @return {@code true} if this map node contains no key&ndash;value pairs; {@code false} otherwise.
	 */

	public boolean isEmpty()
	{
		return pairs.isEmpty();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the number of key&ndash;value pairs that this map node contains.
	 *
	 * @return the number of key&ndash;value pairs that this map node contains.
	 */

	public int getNumPairs()
	{
		return pairs.size();
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this map node contains a key&ndash;value pair with the specified key.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair of interest.
	 * @return {@code true} if this map node contains a key&ndash;value pair whose key is <i>key</i>; {@code false}
	 *         otherwise.
	 */

	public boolean hasKey(String key)
	{
		return pairs.containsKey(key);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this map node contains a key&ndash;value pair whose key is the specified key and whose
	 * value is a {@linkplain NullNode null node}.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair of interest.
	 * @return {@code true} if this map node contains a key&ndash;value pair whose key is <i>key</i> and whose value is
	 *         a {@linkplain NullNode null node}; {@code false} otherwise.
	 */

	public boolean hasNull(String key)
	{
		return pairs.get(key) instanceof NullNode;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this map node contains a key&ndash;value pair whose key is the specified key and whose
	 * value is a {@linkplain BooleanNode Boolean node}.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair of interest.
	 * @return {@code true} if this map node contains a key&ndash;value pair whose key is <i>key</i> and whose value is
	 *         a {@linkplain BooleanNode Boolean node}; {@code false} otherwise.
	 */

	public boolean hasBoolean(String key)
	{
		return pairs.get(key) instanceof BooleanNode;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this map node contains a key&ndash;value pair whose key is the specified key and whose
	 * value is an {@linkplain IntNode 'int' node}.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair of interest.
	 * @return {@code true} if this map node contains a key&ndash;value pair whose key is <i>key</i> and whose value is
	 *         an {@linkplain IntNode 'int' node}; {@code false} otherwise.
	 */

	public boolean hasInt(String key)
	{
		return pairs.get(key) instanceof IntNode;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this map node contains a key&ndash;value pair whose key is the specified key and whose
	 * value is a {@linkplain LongNode 'long' node}.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair of interest.
	 * @return {@code true} if this map node contains a key&ndash;value pair whose key is <i>key</i> and whose value is
	 *         a {@linkplain LongNode 'long' node}; {@code false} otherwise.
	 */

	public boolean hasLong(String key)
	{
		return pairs.get(key) instanceof LongNode;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this map node contains a key&ndash;value pair whose key is the specified key and whose
	 * value is a {@linkplain DoubleNode 'double' node}.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair of interest.
	 * @return {@code true} if this map node contains a key&ndash;value pair whose key is <i>key</i> and whose value is
	 *         a {@linkplain DoubleNode 'double' node}; {@code false} otherwise.
	 */

	public boolean hasDouble(String key)
	{
		return pairs.get(key) instanceof DoubleNode;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this map node contains a key&ndash;value pair whose key is the specified key and whose
	 * value is a {@linkplain StringNode string node}.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair of interest.
	 * @return {@code true} if this map node contains a key&ndash;value pair whose key is <i>key</i> and whose value is
	 *         a {@linkplain StringNode string node}; {@code false} otherwise.
	 */

	public boolean hasString(String key)
	{
		return pairs.get(key) instanceof StringNode;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this map node contains a key&ndash;value pair whose key is the specified key and whose
	 * value is a {@linkplain ListNode list node}.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair of interest.
	 * @return {@code true} if this map node contains a key&ndash;value pair whose key is <i>key</i> and whose value is
	 *         a {@linkplain ListNode list node}; {@code false} otherwise.
	 */

	public boolean hasList(String key)
	{
		return pairs.get(key) instanceof ListNode;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this map node contains a key&ndash;value pair whose key is the specified key and whose
	 * value is a {@linkplain MapNode map node}.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair of interest.
	 * @return {@code true} if this map node contains a key&ndash;value pair whose key is <i>key</i> and whose value is
	 *         a {@linkplain MapNode map node}; {@code false} otherwise.
	 */

	public boolean hasMap(String key)
	{
		return pairs.get(key) instanceof MapNode;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a list of the keys of the key&ndash;value pairs of this map node.  The keys are in the order in which
	 * their associated KV pairs were added to this map node.  The list is independent of this map node, so it may be
	 * modified without affecting this node.
	 *
	 * @return a list of the keys of the key&ndash;value pairs of this map node.
	 * @see    #getChildren()
	 * @see    #getPairList()
	 * @see    #getPairMap()
	 */

	public List<String> getKeys()
	{
		return new ArrayList<>(pairs.keySet());
	}

	//------------------------------------------------------------------

	/**
	 * Returns the value of the key&ndash;value pair of this map node with the specified key.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose value is required.
	 * @return the value of the key&ndash;value pair of this map node whose key is <i>key</i>, or {@code null} if this
	 *         map node does not contain a KV pair with such a key.
	 */

	public AbstractNode getValue(String key)
	{
		return pairs.get(key);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@linkplain NullNode null node} that is associated with the specified key in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose value is required.
	 * @return the null node that is associated with <i>key</i> in this map node, or {@code null} if this map node does
	 *         not contain a KV pair with such a key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           NullNode}.
	 */

	public NullNode getNullNode(String key)
	{
		return (NullNode)pairs.get(key);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@linkplain BooleanNode Boolean node} that is associated with the specified key in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose value is required.
	 * @return the Boolean node that is associated with <i>key</i> in this map node, or {@code null} if this map node
	 *         does not contain a KV pair with such a key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           BooleanNode}.
	 */

	public BooleanNode getBooleanNode(String key)
	{
		return (BooleanNode)pairs.get(key);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying value of the {@linkplain BooleanNode Boolean node} that is associated with the specified
	 * key in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose underlying value is required.
	 * @return the underlying value of the Boolean node that is associated with <i>key</i> in this map node.
	 * @throws NullPointerException
	 *           if this map node does not contain a KV pair with the specified key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           BooleanNode}.
	 */

	public boolean getBoolean(String key)
	{
		return getBooleanNode(key).getValue();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying value of the {@linkplain BooleanNode Boolean node} that is associated with the specified
	 * key in this map node.  If this map node does not contain a key&ndash;value pair with such a key, or if the
	 * associated value is not a Boolean node, the specified default value is returned instead.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose underlying value is required.
	 * @param  defaultValue
	 *           the value that will be returned if this map node does not contain a key&ndash;value pair whose key is
	 *           <i>key</i> or the value of the pair is not a Boolean node.
	 * @return the underlying value of the Boolean node that is associated with <i>key</i> in this map node, or
	 *         <i>defaultValue</i> if there is no such node.
	 */

	public boolean getBoolean(String  key,
							  boolean defaultValue)
	{
		return hasBoolean(key) ? getBooleanNode(key).getValue() : defaultValue;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@linkplain IntNode 'int' node} that is associated with the specified key in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose value is required.
	 * @return the 'int' node that is associated with <i>key</i> in this map node, or {@code null} if this map node
	 *         does not contain a KV pair with such a key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           IntNode}.
	 */

	public IntNode getIntNode(String key)
	{
		return (IntNode)pairs.get(key);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying value of the {@linkplain IntNode 'int' node} that is associated with the specified key
	 * in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose underlying value is required.
	 * @return the underlying value of the 'int' node that is associated with <i>key</i> in this map node.
	 * @throws NullPointerException
	 *           if this map node does not contain a KV pair with the specified key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           IntNode}.
	 */

	public int getInt(String key)
	{
		return getIntNode(key).getValue();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying value of the {@linkplain IntNode 'int' node} that is associated with the specified key
	 * in this map node.  If this map node does not contain a key&ndash;value pair with such a key, or if the associated
	 * value is not an 'int' node, the specified default value is returned instead.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose underlying value is required.
	 * @param  defaultValue
	 *           the value that will be returned if this map node does not contain a key&ndash;value pair whose key is
	 *           <i>key</i> or the value of the pair is not an 'int' node.
	 * @return the underlying value of the 'int' node that is associated with <i>key</i> in this map node, or
	 *         <i>defaultValue</i> if there is no such node.
	 */

	public int getInt(String key,
					  int    defaultValue)
	{
		return hasInt(key) ? getIntNode(key).getValue() : defaultValue;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@linkplain LongNode 'long' node} that is associated with the specified key in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose value is required.
	 * @return the 'long' node that is associated with <i>key</i> in this map node, or {@code null} if this map node
	 *         does not contain a KV pair with such a key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           LongNode}.
	 */

	public LongNode getLongNode(String key)
	{
		return (LongNode)pairs.get(key);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying value of the {@linkplain LongNode 'long' node} that is associated with the specified key
	 * in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose underlying value is required.
	 * @return the underlying value of the 'long' node that is associated with <i>key</i> in this map node.
	 * @throws NullPointerException
	 *           if this map node does not contain a KV pair with the specified key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           LongNode}.
	 */

	public long getLong(String key)
	{
		return getLongNode(key).getValue();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying value of the {@linkplain LongNode 'long' node} that is associated with the specified key
	 * in this map node.  If this map node does not contain a key&ndash;value pair with such a key, or if the associated
	 * value is not a 'long' node, the specified default value is returned instead.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose underlying value is required.
	 * @param  defaultValue
	 *           the value that will be returned if this map node does not contain a key&ndash;value pair whose key is
	 *           <i>key</i> or the value of the pair is not a 'long' node.
	 * @return the underlying value of the 'long' node that is associated with <i>key</i> in this map node, or
	 *         <i>defaultValue</i> if there is no such node.
	 */

	public long getLong(String key,
						long   defaultValue)
	{
		return hasLong(key) ? getLongNode(key).getValue() : defaultValue;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@linkplain DoubleNode 'double' node} that is associated with the specified key in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose value is required.
	 * @return the 'double' node that is associated with <i>key</i> in this map node, or {@code null} if this map node
	 *         does not contain a KV pair with such a key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           DoubleNode}.
	 */

	public DoubleNode getDoubleNode(String key)
	{
		return (DoubleNode)pairs.get(key);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying value of the {@linkplain DoubleNode 'double' node} that is associated with the specified
	 * key in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose underlying value is required.
	 * @return the underlying value of the 'double' node that is associated with <i>key</i> in this map node.
	 * @throws NullPointerException
	 *           if this map node does not contain a KV pair with the specified key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           DoubleNode}.
	 */

	public double getDouble(String key)
	{
		return getDoubleNode(key).getValue();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying value of the {@linkplain DoubleNode 'double' node} that is associated with the specified
	 * key in this map node.  If this map node does not contain a key&ndash;value pair with such a key, or if the
	 * associated value is not a 'double' node, the specified default value is returned instead.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose underlying value is required.
	 * @param  defaultValue
	 *           the value that will be returned if this map node does not contain a key&ndash;value pair whose key is
	 *           <i>key</i> or the value of the pair is not a 'double' node.
	 * @return the underlying value of the 'double' node that is associated with <i>key</i> in this map node, or
	 *         <i>defaultValue</i> if there is no such node.
	 */

	public double getDouble(String key,
							double defaultValue)
	{
		return hasDouble(key) ? getDoubleNode(key).getValue() : defaultValue;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@linkplain StringNode string node} that is associated with the specified key in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose value is required.
	 * @return the string node that is associated with <i>key</i> in this map node, or {@code null} if this map node
	 *         does not contain a KV pair with such a key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           StringNode}.
	 */

	public StringNode getStringNode(String key)
	{
		return (StringNode)pairs.get(key);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying value of the {@linkplain StringNode string node} that is associated with the specified key
	 * in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose underlying value is required.
	 * @return the underlying value of the string node that is associated with <i>key</i> in this map node.
	 * @throws NullPointerException
	 *           if this map node does not contain a KV pair with the specified key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           StringNode}.
	 */

	public String getString(String key)
	{
		return getStringNode(key).getValue();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the underlying value of the {@linkplain StringNode string node} that is associated with the specified key
	 * in this map node.  If this map node does not contain a key&ndash;value pair with such a key, or if the associated
	 * value is not a string node, the specified default value is returned instead.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose underlying value is required.
	 * @param  defaultValue
	 *           the value that will be returned if this map node does not contain a key&ndash;value pair whose key is
	 *           <i>key</i> or the value of the pair is not a string node.
	 * @return the underlying value of the string node that is associated with <i>key</i> in this map node, or
	 *         <i>defaultValue</i> if there is no such node.
	 */

	public String getString(String key,
							String defaultValue)
	{
		return hasString(key) ? getStringNode(key).getValue() : defaultValue;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@linkplain ListNode list node} that is associated with the specified key in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose value is required.
	 * @return the list node that is associated with <i>key</i> in this map node, or {@code null} if this map node does
	 *         not contain a KV pair with such a key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           ListNode}.
	 */

	public ListNode getListNode(String key)
	{
		return (ListNode)pairs.get(key);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@linkplain MapNode map node} that is associated with the specified key in this map node.
	 *
	 * @param  key
	 *           the key of the key&ndash;value pair whose value is required.
	 * @return the map node that is associated with <i>key</i> in this map node, or {@code null} if this map node does
	 *         not contain a KV pair with such a key.
	 * @throws ClassCastException
	 *           if this map node contains a KV pair with the specified key and its value is not an instance of {@link
	 *           MapNode}.
	 */

	public MapNode getMapNode(String key)
	{
		return (MapNode)pairs.get(key);
	}

	//------------------------------------------------------------------

	/**
	 * Returns a list of the key&ndash;value pairs of this map node.  Iterating over the list will traverse the KV pairs
	 * in the order in which they were added to this map node.  Modifying the value of a KV pair (for example, changing
	 * the parent of a node) will affect this map node.
	 *
	 * @return a list of the key&ndash;value pairs of this map node.
	 * @see    #getPairMap()
	 * @see    #getChildren()
	 * @see    #getKeys()
	 */

	public List<Pair> getPairList()
	{
		List<Pair> pairs = new ArrayList<>();
		for (Map.Entry<String, AbstractNode> entry : this.pairs.entrySet())
			pairs.add(new Pair(entry.getKey(), entry.getValue()));
		return pairs;
	}

	//------------------------------------------------------------------

	/**
	 * Returns an unmodifiable map of the key&ndash;value pairs of this map node.  Iterating over the {@linkplain
	 * Map#entrySet() entries} of the map will traverse the KV pairs in the order in which they were added to this map
	 * node.  Although the returned map cannot be modified, the values of its KV pairs <i>can</i> be modified, and doing
	 * so (for example, changing the parent of a node) will affect this map node.
	 *
	 * @return an unmodifiable map of the key&ndash;value pairs of this map node.
	 * @see    #getPairList()
	 * @see    #getChildren()
	 * @see    #getKeys()
	 */

	public Map<String, AbstractNode> getPairMap()
	{
		return Collections.unmodifiableMap(pairs);
	}

	//------------------------------------------------------------------

	/**
	 * Returns an iterator over the key&ndash;value pairs of this map node.
	 *
	 * @return an iterator over the key&ndash;value pairs of this map node.
	 */

	public Iterator<Map.Entry<String, AbstractNode>> getPairIterator()
	{
		return pairs.entrySet().iterator();
	}

	//------------------------------------------------------------------

	/**
	 * Sets the key&ndash;value pairs of this map node to the specified pairs of keys and {@linkplain AbstractNode
	 * values}.
	 *
	 * @param pairs
	 *          the pairs of keys and values to which the key&ndash;value pairs of this map node will be set.
	 */

	public void setPairs(Map<String, AbstractNode> pairs)
	{
		this.pairs.clear();
		addPairs(pairs);
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified key&ndash;value pair to this map node.  If this map node already contains a KV pair with the
	 * key of the new pair, the new pair will replace the existing pair without affecting the order of the pairs;
	 * otherwise, the specified pair will be added to end of the collection of pairs.
	 *
	 * @param pair
	 *          the key&ndash;value pair that will be added to this map node.
	 * @throws IllegalArgumentException
	 *          if <i>pair</i> is {@code null}.
	 */

	public void addPair(Pair pair)
	{
		addPair(pair.key, pair.value);
	}

	//------------------------------------------------------------------

	/**
	 * Adds a key&ndash;value pair with the specified key and value to this map node.  If this map node already contains
	 * a KV pair with the specified key, the specified value will replace the value of the existing pair without
	 * affecting the order of the pairs; otherwise, a new KV pair will be added to end of the collection of pairs.
	 *
	 * @param key
	 *          the key of the key&ndash;value pair.
	 * @param value
	 *          the value of the key&ndash;value pair.
	 */

	public void addPair(String       key,
						AbstractNode value)
	{
		pairs.put(key, value);
		value.setParent(this);
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified key&ndash;value pairs to the key&ndash;value pairs of this map node.  If this map node already
	 * contains a KV pair with the key of one of the new pairs, the value of the new pair will replace the value of the
	 * existing pair without affecting the order of the pairs; otherwise, the new KV pair will be added to end of the
	 * collection of pairs.  The KV pairs are added in the order in which they are traversed by their iterator.
	 *
	 * @param pairs
	 *          the key&ndash;value pairs that will be added to the key&ndash;value pairs of this map node.
	 */

	public void addPairs(Pair... pairs)
	{
		for (Pair pair : pairs)
			addPair(pair);
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified key&ndash;value pairs to the key&ndash;value pairs of this map node.  If this map node already
	 * contains a KV pair with the key of one of the new pairs, the value of the new pair will replace the value of the
	 * existing pair without affecting the order of the pairs; otherwise, the new KV pair will be added to end of the
	 * collection of pairs.  The KV pairs are added in the order in which they are traversed by their iterator.
	 *
	 * @param pairs
	 *          the key&ndash;value pairs that will be added to the key&ndash;value pairs of this map node.
	 */

	public void addPairs(Iterable<? extends Pair> pairs)
	{
		for (Pair pair : pairs)
			addPair(pair);
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified pairs of keys and {@linkplain AbstractNode values} to the key&ndash;value pairs of this map
	 * node.  If this map node already contains a KV pair with the key of one of the new pairs, the value of the new
	 * pair will replace the value of the existing pair without affecting the order of the pairs; otherwise, the new KV
	 * pair will be added to end of the collection of pairs.  The KV pairs are added in the order in which they are
	 * traversed by the iterator over the entries of the input map.
	 *
	 * @param pairs
	 *          the pairs of keys and values that will be added to the key&ndash;value pairs of this map node.
	 */

	public void addPairs(Map<String, AbstractNode> pairs)
	{
		for (Map.Entry<String, AbstractNode> pair : pairs.entrySet())
			addPair(pair.getKey(), pair.getValue());
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain NullNode null node}, adds it to this map node as a key&ndash;value pair
	 * with the specified key and returns it.  If this map node already contains a KV pair with the specified key, the
	 * new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a new
	 * KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new null node will be associated.
	 * @return the null node that was created and added to this map node as a key&ndash;value pair whose key is
	 *         <i>key</i>.
	 */

	public NullNode addNull(String key)
	{
		NullNode node = new NullNode();
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain BooleanNode Boolean node}, adds it to this map node as a key&ndash;value
	 * pair with the specified key and returns it.  If this map node already contains a KV pair with the specified key,
	 * the new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a
	 * new KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new Boolean node will be associated.
	 * @param  value
	 *           the value of the new Boolean node.
	 * @return the Boolean node that was created and added to this map node as a key&ndash;value pair whose key is
	 *         <i>key</i>.
	 */

	public BooleanNode addBoolean(String  key,
								  boolean value)
	{
		BooleanNode node = new BooleanNode(value);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} whose elements are new instances of {@linkplain
	 * BooleanNode Boolean nodes} with the specified values, adds the list node to this map node as a key&ndash;value
	 * pair with the specified key and returns it.  If this map node already contains a KV pair with the specified key,
	 * the new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a
	 * new KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  values
	 *           the values for which a list node containing Boolean nodes will be created.
	 * @return the new list node that contains the Boolean nodes that were created from <i>values</i> and that was added
	 *         to this map node as a key&ndash;value pair whose key is <i>key</i>.
	 */

	public ListNode addBooleans(String     key,
								boolean... values)
	{
		ListNode node = new ListNode();
		node.addBooleans(values);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} whose elements are new instances of {@linkplain
	 * BooleanNode Boolean nodes} with the specified values, adds the list node to this map node as a key&ndash;value
	 * pair with the specified key and returns it.  If this map node already contains a KV pair with the specified key,
	 * the new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a
	 * new KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  values
	 *           the values for which a list node containing Boolean nodes will be created.
	 * @return the new list node that contains the Boolean nodes that were created from <i>values</i> and that was added
	 *         to this map node as a key&ndash;value pair whose key is <i>key</i>.
	 */

	public ListNode addBooleans(String            key,
								Iterable<Boolean> values)
	{
		ListNode node = new ListNode();
		node.addBooleans(values);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of an {@linkplain IntNode 'int' node}, adds it to this map node as a key&ndash;value pair
	 * with the specified key and returns it.  If this map node already contains a KV pair with the specified key, the
	 * new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a new
	 * KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new 'int' node will be associated.
	 * @param  value
	 *           the value of the new 'int' node.
	 * @return the 'int' node that was created and added to this map node as a key&ndash;value pair whose key is
	 *         <i>key</i>.
	 */

	public IntNode addInt(String key,
						  int    value)
	{
		IntNode node = new IntNode(value);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} whose elements are new instances of {@linkplain
	 * IntNode 'int' nodes} with the specified values, adds the list node to this map node as a key&ndash;value pair
	 * with the specified key and returns it.  If this map node already contains a KV pair with the specified key, the
	 * new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a new
	 * KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  values
	 *           the values for which a list node containing 'int' nodes will be created.
	 * @return the new list node that contains the 'int' nodes that were created from <i>values</i> and that was added
	 *         to this map node as a key&ndash;value pair whose key is <i>key</i>.
	 */

	public ListNode addInts(String key,
							int... values)
	{
		ListNode node = new ListNode();
		node.addInts(values);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} whose elements are new instances of {@linkplain
	 * IntNode 'int' nodes} with the specified values, adds the list node to this map node as a key&ndash;value pair
	 * with the specified key and returns it.  If this map node already contains a KV pair with the specified key, the
	 * new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a new
	 * KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  values
	 *           the values for which a list node containing 'int' nodes will be created.
	 * @return the new list node that contains the 'int' nodes that were created from <i>values</i> and that was added
	 *         to this map node as a key&ndash;value pair whose key is <i>key</i>.
	 */

	public ListNode addInts(String            key,
							Iterable<Integer> values)
	{
		ListNode node = new ListNode();
		node.addInts(values);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of an {@linkplain LongNode 'long' node}, adds it to this map node as a key&ndash;value
	 * pair with the specified key and returns it.  If this map node already contains a KV pair with the specified key,
	 * the new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a
	 * new KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new 'long' node will be associated.
	 * @param  value
	 *           the value of the new 'long' node.
	 * @return the 'long' node that was created and added to this map node as a key&ndash;value pair whose key is
	 *         <i>key</i>.
	 */

	public LongNode addLong(String key,
							long   value)
	{
		LongNode node = new LongNode(value);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} whose elements are new instances of {@linkplain
	 * LongNode 'long' nodes} with the specified values, adds the list node to this map node as a key&ndash;value pair
	 * with the specified key and returns it.  If this map node already contains a KV pair with the specified key, the
	 * new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a new
	 * KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  values
	 *           the values for which a list node containing 'long' nodes will be created.
	 * @return the new list node that contains the 'long' nodes that were created from <i>values</i> and that was added
	 *         to this map node as a key&ndash;value pair whose key is <i>key</i>.
	 */

	public ListNode addLongs(String  key,
							 long... values)
	{
		ListNode node = new ListNode();
		node.addLongs(values);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} whose elements are new instances of {@linkplain
	 * LongNode 'long' nodes} with the specified values, adds the list node to this map node as a key&ndash;value pair
	 * with the specified key and returns it.  If this map node already contains a KV pair with the specified key, the
	 * new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a new
	 * KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  values
	 *           the values for which a list node containing 'long' nodes will be created.
	 * @return the new list node that contains the 'long' nodes that were created from <i>values</i> and that was added
	 *         to this map node as a key&ndash;value pair whose key is <i>key</i>.
	 */

	public ListNode addLongs(String         key,
							 Iterable<Long> values)
	{
		ListNode node = new ListNode();
		node.addLongs(values);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of an {@linkplain DoubleNode 'double' node}, adds it to this map node as a key&ndash;value
	 * pair with the specified key and returns it.  If this map node already contains a KV pair with the specified key,
	 * the new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a
	 * new KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new 'double' node will be associated.
	 * @param  value
	 *           the value of the new 'double' node.
	 * @return the 'double' node that was created and added to this map node as a key&ndash;value pair whose key is
	 *         <i>key</i>.
	 */

	public DoubleNode addDouble(String key,
								double value)
	{
		DoubleNode node = new DoubleNode(value);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} whose elements are new instances of {@linkplain
	 * DoubleNode 'double' nodes} with the specified values, adds the list node to this map node as a key&ndash;value
	 * pair with the specified key and returns it.  If this map node already contains a KV pair with the specified key,
	 * the new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a
	 * new KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  values
	 *           the values for which a list node containing 'double' nodes will be created.
	 * @return the new list node that contains the 'double' nodes that were created from <i>values</i> and that was
	 *         added to this map node as a key&ndash;value pair whose key is <i>key</i>.
	 */

	public ListNode addDoubles(String    key,
							   double... values)
	{
		ListNode node = new ListNode();
		node.addDoubles(values);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} whose elements are new instances of {@linkplain
	 * DoubleNode 'double' nodes} with the specified values, adds the list node to this map node as a key&ndash;value
	 * pair with the specified key and returns it.  If this map node already contains a KV pair with the specified key,
	 * the new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a
	 * new KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  values
	 *           the values for which a list node containing 'double' nodes will be created.
	 * @return the new list node that contains the 'double' nodes that were created from <i>values</i> and that was
	 *         added to this map node as a key&ndash;value pair whose key is <i>key</i>.
	 */

	public ListNode addDoubles(String           key,
							   Iterable<Double> values)
	{
		ListNode node = new ListNode();
		node.addDoubles(values);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of an {@linkplain StringNode string node}, adds it to this map node as a key&ndash;value
	 * pair with the specified key and returns it.  If this map node already contains a KV pair with the specified key,
	 * the new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a
	 * new KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new string node will be associated.
	 * @param  value
	 *           the value of the new string node.
	 * @return the string node that was created and added to this map node as a key&ndash;value pair whose key is
	 *         <i>key</i>.
	 */

	public StringNode addString(String key,
								String value)
	{
		StringNode node = new StringNode(value);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} whose elements are new instances of {@linkplain
	 * StringNode string nodes} with the specified values, adds the list node to this map node as a key&ndash;value pair
	 * with the specified key and returns it.  If this map node already contains a KV pair with the specified key, the
	 * new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a new
	 * KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  values
	 *           the values for which a list node containing string nodes will be created.
	 * @return the new list node that contains the string nodes that were created from <i>values</i> and that was added
	 *         to this map node as a key&ndash;value pair whose key is <i>key</i>.
	 */

	public ListNode addStrings(String    key,
							   String... values)
	{
		ListNode node = new ListNode();
		node.addStrings(values);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} whose elements are new instances of {@linkplain
	 * StringNode string nodes} with the specified values, adds the list node to this map node as a key&ndash;value pair
	 * with the specified key and returns it.  If this map node already contains a KV pair with the specified key, the
	 * new node will replace the value of the existing pair without affecting the order of the pairs; otherwise, a new
	 * KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  values
	 *           the values for which a list node containing string nodes will be created.
	 * @return the new list node that contains the string nodes that were created from <i>values</i> and that was added
	 *         to this map node as a key&ndash;value pair whose key is <i>key</i>.
	 */

	public ListNode addStrings(String           key,
							   Iterable<String> values)
	{
		ListNode node = new ListNode();
		node.addStrings(values);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} that contains the specified elements, adds it to this
	 * map node as a key&ndash;value pair with the specified key and returns it.  If this map node already contains a KV
	 * pair with the specified key, the new node will replace the value of the existing pair without affecting the order
	 * of the pairs; otherwise, a new KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  elements
	 *           the elements of the new list node.
	 * @return the new list node that contains <i>elements</i> and that was added to this map node as a key&ndash;value
	 *         pair whose key is <i>key</i>.
	 */

	public ListNode addList(String          key,
							AbstractNode... elements)
	{
		ListNode node = new ListNode(Arrays.asList(elements));
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain ListNode list node} that contains the specified elements, adds it to this
	 * map node as a key&ndash;value pair with the specified key and returns it.  If this map node already contains a KV
	 * pair with the specified key, the new node will replace the value of the existing pair without affecting the order
	 * of the pairs; otherwise, a new KV pair will be added to end of the collection of pairs.
	 *
	 * @param  key
	 *           the key with which the new list node will be associated.
	 * @param  elements
	 *           the elements of the new list node.
	 * @return the new list node that contains <i>elements</i> and that was added to this map node as a key&ndash;value
	 *         pair whose key is <i>key</i>.
	 */

	public ListNode addList(String                           key,
							Iterable<? extends AbstractNode> elements)
	{
		ListNode node = new ListNode(elements);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain MapNode map node} that contains the specified pairs of keys and values,
	 * adds the new map node to this map node as a key&ndash;value pair with the specified key and returns it.  If this
	 * map node already contains a KV pair with the specified key, the new node will replace the value of the existing
	 * pair without affecting the order of the pairs; otherwise, a new KV pair will be added to end of the collection of
	 * pairs.
	 *
	 * @param  key
	 *           the key with which the new map node will be associated.
	 * @param  pairs
	 *           the key&ndash;value pairs of the new map node.
	 * @return the new map node that contains <i>pairs</i> and that was added to this map node as a key&ndash;value pair
	 *         whose key is <i>key</i>.
	 */

	public MapNode addMap(String  key,
						  Pair... pairs)
	{
		MapNode node = new MapNode(pairs);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain MapNode map node} that contains the specified pairs of keys and values,
	 * adds the new map node to this map node as a key&ndash;value pair with the specified key and returns it.  If this
	 * map node already contains a KV pair with the specified key, the new node will replace the value of the existing
	 * pair without affecting the order of the pairs; otherwise, a new KV pair will be added to end of the collection of
	 * pairs.
	 *
	 * @param  key
	 *           the key with which the new map node will be associated.
	 * @param  pairs
	 *           the key&ndash;value pairs of the new map node.
	 * @return the new map node that contains <i>pairs</i> and that was added to this map node as a key&ndash;value pair
	 *         whose key is <i>key</i>.
	 */

	public MapNode addMap(String                   key,
						  Iterable<? extends Pair> pairs)
	{
		MapNode node = new MapNode(pairs);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a {@linkplain MapNode map node} that contains the specified pairs of keys and values,
	 * adds the new map node to this map node as a key&ndash;value pair with the specified key and returns it.  If this
	 * map node already contains a KV pair with the specified key, the new node will replace the value of the existing
	 * pair without affecting the order of the pairs; otherwise, a new KV pair will be added to end of the collection of
	 * pairs.
	 *
	 * @param  key
	 *           the key with which the new map node will be associated.
	 * @param  pairs
	 *           the key&ndash;value pairs of the new map node.
	 * @return the new map node that contains <i>pairs</i> and that was added to this map node as a key&ndash;value pair
	 *         whose key is <i>key</i>.
	 */

	public MapNode addMap(String                    key,
						  Map<String, AbstractNode> pairs)
	{
		MapNode node = new MapNode(pairs);
		addPair(key, node);
		return node;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of the specified key for a map node.
	 *
	 * @param  key
	 *           the key whose string representation is required.
	 * @return the string representation of <i>key</i>.
	 */

	public String keyToString(CharSequence key)
	{
		return StringNode.escapeAndQuote(key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	/** A map of the key&ndash;value pairs of this map node. */
	private	LinkedHashMap<String, AbstractNode>	pairs;

}

//----------------------------------------------------------------------
