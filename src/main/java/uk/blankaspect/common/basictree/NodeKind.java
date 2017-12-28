/*====================================================================*\

NodeKind.java

Enumeration: kind of node.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.basictree;

//----------------------------------------------------------------------


// ENUMERATION: KIND OF NODE


/**
 * This is an enumeration of the kinds of {@linkplain AbstractNode node}.
 */

public enum NodeKind
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/**
	 * The node represents a null.
	 */
	NULL,

	/**
	 * The node contains a Boolean value.
	 */
	BOOLEAN,

	/**
	 * The node contains an integer.
	 */
	INT,

	/**
	 * The node contains a long integer.
	 */
	LONG,

	/**
	 * The node contains a double-precision floating-point number.
	 */
	DOUBLE,

	/**
	 * The node contains a string.
	 */
	STRING,

	/**
	 * The node contains a list of nodes.
	 */
	LIST,

	/**
	 * The node contains a map of nodes.
	 */
	MAP

}

//----------------------------------------------------------------------
