/*====================================================================*\

DoubleNode.java

Class: double node.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.basictree;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;

//----------------------------------------------------------------------


// CLASS: DOUBLE NODE


/**
 * This class implements a {@linkplain AbstractNode node} that contains a double-precision floating-point number.
 */

public class DoubleNode
	extends AbstractNode
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The type of a 'double' node. */
	public static final	NodeType	TYPE	= new NodeType(NodeType.ANY, DoubleNode.class);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The value of this 'double' node. */
	private	double	value;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a 'double' node that has no parent and has the specified value.
	 *
	 * @param value
	 *          the value of the 'double' node.
	 */

	public DoubleNode(double value)
	{
		// Call alternative constructor
		this(null, value);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a 'double' node that has the specified parent and value.
	 *
	 * @param parent
	 *          the parent of the 'double' node.
	 * @param value
	 *          the value of the 'double' node.
	 */

	public DoubleNode(AbstractNode parent,
					  double       value)
	{
		// Call superclass constructor
		super(parent);

		// Initialise instance variables
		this.value = value;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a list of 'double' nodes for the specified values, preserving the order of the elements, and returns the
	 * list, which may be used to construct a {@linkplain ListNode list node}.
	 *
	 * @param  values
	 *           the values for which 'double' nodes will be created.
	 * @return a list of 'double' nodes whose underlying values are <i>values</i>.
	 */

	public static List<DoubleNode> valuesToNodes(double... values)
	{
		List<DoubleNode> outValues = new ArrayList<>();
		for (double value : values)
			outValues.add(new DoubleNode(value));
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a list of 'double' nodes for the specified values, preserving the order of the elements, and returns the
	 * list, which may be used to construct a {@linkplain ListNode list node}.
	 *
	 * @param  values
	 *           the values for which 'double' nodes will be created.
	 * @return a list of 'double' nodes whose underlying values are <i>values</i>.
	 */

	public static List<DoubleNode> valuesToNodes(Iterable<Double> values)
	{
		List<DoubleNode> outValues = new ArrayList<>();
		for (Double value : values)
			outValues.add(new DoubleNode(value));
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
	 * For a 'double' node, this method always returns {@code false}.
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
	 * Returns {@code true} if the specified object is an instance of {@code DoubleNode} <i>and</i> it has the same
	 * value as this 'double' node.
	 *
	 * @param  obj
	 *           the object with which this 'double' node will be compared.
	 * @return {@code true} if <i>obj</i> is an instance of {@code DoubleNode} <i>and</i> it has the same value as this
	 *         'double' node; {@code false} otherwise.
	 */

	@Override
	public boolean equals(Object obj)
	{
		return (obj == this) || ((obj instanceof DoubleNode) && (value == ((DoubleNode)obj).value));
	}

	//------------------------------------------------------------------

	/**
	 * Returns the hash code of this 'double' node.
	 *
	 * @return the hash code of this 'double' node.
	 */

	@Override
	public int hashCode()
	{
		return Double.hashCode(value);
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a copy of this 'double' node that has no parent.
	 *
	 * @return a copy of this 'double' node that has no parent.
	 */

	@Override
	public DoubleNode clone()
	{
		return (DoubleNode)super.clone();
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of this 'double' node.
	 *
	 * @return a string representation of this 'double' node.
	 */

	@Override
	public String toString()
	{
		return Double.toString(value);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the value of this 'double' node.
	 *
	 * @return the value of this 'double' node.
	 */

	public double getValue()
	{
		return value;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
