/*====================================================================*\

IntNode.java

Class: integer node.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.basictree;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;

//----------------------------------------------------------------------


// CLASS: INTEGER NODE


/**
 * This class implements a {@linkplain AbstractNode node} that contains an integer.
 */

public class IntNode
	extends AbstractNode
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The type of an 'int' node. */
	public static final	NodeType	TYPE	= new NodeType(AbstractNode.TYPE, IntNode.class);

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of an 'int' node that has no parent and has the specified value.
	 *
	 * @param value
	 *          the value of the 'int' node.
	 */

	public IntNode(int value)
	{
		// Call alternative constructor
		this(null, value);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of an 'int' node that has the specified parent and value.
	 *
	 * @param parent
	 *          the parent of the 'int' node.
	 * @param value
	 *          the value of the 'int' node.
	 */

	public IntNode(AbstractNode parent,
				   int          value)
	{
		// Call superclass constructor
		super(parent);

		// Initialise instance fields
		this.value = value;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a list of 'int' nodes for the specified values, preserving the order of the elements, and returns the
	 * list, which may be used to construct a {@linkplain ListNode list node}.
	 *
	 * @param  values
	 *           the values for which 'int' nodes will be created.
	 * @return a list of 'int' nodes whose underlying values are <i>values</i>.
	 */

	public static List<IntNode> valuesToNodes(int... values)
	{
		List<IntNode> outValues = new ArrayList<>();
		for (int value : values)
			outValues.add(new IntNode(value));
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a list of 'int' nodes for the specified values, preserving the order of the elements, and returns the
	 * list, which may be used to construct a {@linkplain ListNode list node}.
	 *
	 * @param  values
	 *           the values for which 'int' nodes will be created.
	 * @return a list of 'int' nodes whose underlying values are <i>values</i>.
	 */

	public static List<IntNode> valuesToNodes(Iterable<Integer> values)
	{
		List<IntNode> outValues = new ArrayList<>();
		for (Integer value : values)
			outValues.add(new IntNode(value));
		return outValues;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @return {@link #TYPE}.
	 */

	@Override
	public NodeType getType()
	{
		return TYPE;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * For an 'int' node, this method always returns {@code false}.
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
	 * Returns {@code true} if the specified object is an instance of {@code IntNode} <i>and</i> it has the same value
	 * as this 'int' node.
	 *
	 * @param  obj
	 *           the object with which this 'int' node will be compared.
	 * @return {@code true} if <i>obj</i> is an instance of {@code IntNode} <i>and</i> it has the same value as this
	 *         'int' node; {@code false} otherwise.
	 */

	@Override
	public boolean equals(Object obj)
	{
		return (obj == this) || ((obj instanceof IntNode) && (value == ((IntNode)obj).value));
	}

	//------------------------------------------------------------------

	/**
	 * Returns the hash code of this 'int' node.
	 *
	 * @return the hash code of this 'int' node.
	 */

	@Override
	public int hashCode()
	{
		return Integer.hashCode(value);
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a copy of this 'int' node that has no parent.
	 *
	 * @return a copy of this 'int' node that has no parent.
	 */

	@Override
	public IntNode clone()
	{
		return (IntNode)super.clone();
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of this 'int' node.
	 *
	 * @return a string representation of this 'int' node.
	 */

	@Override
	public String toString()
	{
		return Integer.toString(value);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the value of this 'int' node.
	 *
	 * @return the value of this 'int' node.
	 */

	public int getValue()
	{
		return value;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	/** The value of this 'int' node. */
	private	int	value;

}

//----------------------------------------------------------------------
