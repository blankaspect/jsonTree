/*====================================================================*\

JsonArray.java

Interface: JSON array.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// INTERFACE: JSON ARRAY


/**
 * This interface defines constants that relate to JSON arrays.
 */

public interface JsonArray
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The character that denotes the start of a JSON array. */
	char	START_CHAR	= '[';

	/** The character that denotes the end of a JSON array. */
	char	END_CHAR	= ']';

	/** The character that separates adjacent elements of a JSON array. */
	char	ELEMENT_SEPARATOR_CHAR	= ',';

}

//----------------------------------------------------------------------
